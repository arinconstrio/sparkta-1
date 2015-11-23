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

package com.stratio.sparkta.driver.test.factory

import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.serving.core.SparktaConfig
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkContext
import org.apache.spark.streaming.Duration
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FlatSpec, _}

@RunWith(classOf[JUnitRunner])
class SparkContextFactoryTest extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
  self: FlatSpec =>

  override def afterAll {
    SparkContextFactory.destroySparkContext
  }

  object LocalSparkContext {

    def getNewLocalSparkContext(numExecutors: Int = 1, title: String): SparkContext =
      new SparkContext(s"local[$numExecutors]", title)
  }

  trait WithConfig {

    val config = SparktaConfig.initConfig("sparkta.local")
    val wrongConfig = ConfigFactory.empty
    val seconds = 6
    val batchDuraction = Duration(seconds)
    val specificConfig = Map("spark.driver.allowMultipleContexts" -> "true")
  }

  "SparkContextFactorySpec" should "fails when properties is missing" in new WithConfig {
    an[Exception] should be thrownBy (SparkContextFactory.sparkStandAloneContextInstance(None, specificConfig, Seq()))
  }

  it should "create and reuse same context" in new WithConfig {
    val sc = SparkContextFactory.sparkStandAloneContextInstance(config, specificConfig, Seq())
    val otherSc = SparkContextFactory.sparkStandAloneContextInstance(config, specificConfig, Seq())
    sc should be equals (otherSc)
    SparkContextFactory.destroySparkContext
  }

  it should "create and reuse same SQLContext" in new WithConfig {
    val sc = SparkContextFactory.sparkStandAloneContextInstance(config, specificConfig, Seq())
    val sqc = SparkContextFactory.sparkSqlContextInstance
    sqc shouldNot be equals (None)
    val otherSqc = SparkContextFactory.sparkSqlContextInstance
    sqc should be equals (otherSqc)
    SparkContextFactory.destroySparkContext
  }

  it should "create and reuse same SparkStreamingContext" in new WithConfig {
    val checkpointDir = "checkpoint/SparkContextFactorySpec"
    val sc = SparkContextFactory.sparkStandAloneContextInstance(config, specificConfig, Seq())
    SparkContextFactory.sparkStreamingInstance should be(None)
    val ssc = SparkContextFactory.sparkStreamingInstance(batchDuraction, checkpointDir)
    ssc shouldNot be equals (None)
    val otherSsc = SparkContextFactory.sparkStreamingInstance(batchDuraction, checkpointDir)
    ssc should be equals (otherSsc)
  }
}
