/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.driver

import java.io.File
import java.net.URI
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils

import com.stratio.sparkta.driver.SparktaJob._
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.driver.util.{HdfsUtils, PolicyUtils}
import com.stratio.sparkta.serving.core.helpers.JarsHelper
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, PolicyStatusModel, SparktaSerializer}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparkta.serving.core.policy.status.{PolicyStatusActor, PolicyStatusEnum}
import com.stratio.sparkta.serving.core.utils.HexUtils
import com.stratio.sparkta.serving.core.{AppConstant, CuratorFactoryHolder, SparktaConfig}

object SparktaClusterJob extends SparktaSerializer {

  implicit val timeout: Timeout = Timeout(3.seconds)

  def main(args: Array[String]): Unit = {
    if (checkArgs(args)) {
      Try {
        val zk = HexUtils.hex2string(args(3))
        val cfgString = HexUtils.hex2string(args(4))
        initSparktaConfig(cfgString, zk)
        val policy = getPolicyFromZookeeper(args(0))
        val hadoopUserName = scala.util.Properties.envOrElse("HADOOP_USER_NAME", AppConstant.DefaultHadoopUserName)
        val hdfsUgi=HdfsUtils.ugi(hadoopUserName)
        val hadoopConfDir = scala.util.Properties.envOrNone("HADOOP_CONF_DIR")
        val hdfsConf=HdfsUtils.hdfsConfiguration(hadoopConfDir)
        val hdfsUtils = new HdfsUtils(hdfsUgi, hdfsConf)
        val pluginFiles = addHdfsFiles(hdfsUtils, args(1))
        val classPathFiles = addHdfsFiles(hdfsUtils, args(2))
        implicit val system = ActorSystem(s"${policy.id.get}")
        val policyStatusActor = system.actorOf(Props[PolicyStatusActor], "supervisorContextActor")
        Try {
          policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Starting))
          val streamingContextService = new StreamingContextService(Some(policyStatusActor))
          val ssc = streamingContextService.clusterStreamingContext(
            policy, pluginFiles ++ classPathFiles, Map("spark.app.name" -> s"${policy.name}")).get
          ssc.start
        } match {
          case Success(_) => {
            policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Started))
            log.info(s"Starting Streaming Context for policy ${policy.id.get}")
          }
          case Failure(exception) => {
            log.error(exception.getLocalizedMessage, exception)
            policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Failed))
          }
        }
      } match {
        case Success(_) => {
          log.info("Policy started")
        }
        case Failure(exception) => {
          log.error("Creating classpath and StatusActor")
          log.error(exception.getLocalizedMessage, exception)
        }
      }
    } else {
      log.error("Invalid arguments")
      System.exit(-1)
    }
  }

  def cleanCommas(value: String): String = value.stripPrefix(",").stripSuffix(",")

  def initSparktaConfig(detailConfig: String, zkConfig: String): Unit = {
    val detail = ConfigFactory.parseString(cleanCommas(detailConfig))
    val zk = ConfigFactory.parseString(cleanCommas(zkConfig))
    val merged = detail.withFallback(zk).atKey("sparkta")
    SparktaConfig.initMainConfig(Some(merged))
  }

  def addHdfsFiles(hdfsUtils: HdfsUtils, hdfsPath: String): Array[URI] = {
    hdfsUtils.getFiles(hdfsPath).map(file => {
      val fileName = s"$hdfsPath${file.getPath.getName}"
      val tempFile = File.createTempFile(file.getPath.getName, "")

      FileUtils.copyInputStreamToFile(hdfsUtils.getFile(fileName), tempFile)
      JarsHelper.addToClasspath(tempFile)
      file.getPath.toUri
    })
  }

  def getPolicyFromZookeeper(policyId: String): AggregationPoliciesModel = {
    Try {
      val curatorFramework = CuratorFactoryHolder.getInstance()
      PolicyUtils.parseJson(new Predef.String(curatorFramework.getData.forPath(
        s"${AppConstant.PoliciesBasePath}/${policyId}")))
    } match {
      case Success(policy) => policy
      case Failure(e) => log.error(s"Cannot load policy $policyId", e); throw e
    }
  }

  def checkArgs(args: Array[String]): Boolean = args.length == 5
}
