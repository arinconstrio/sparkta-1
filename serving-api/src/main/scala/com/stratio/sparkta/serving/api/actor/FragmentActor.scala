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

package com.stratio.sparkta.serving.api.actor

import java.util.UUID

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.serving.api.actor.FragmentActor._
import com.stratio.sparkta.serving.api.exception.ServingApiException
import com.stratio.sparkta.serving.core.AppConstant
import com.stratio.sparkta.serving.core.models.{ErrorModel, FragmentElementModel, SparktaSerializer}
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException
import org.json4s.jackson.Serialization.{read, write}
import spray.httpx.Json4sJacksonSupport

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

class FragmentActor(curatorFramework: CuratorFramework)
  extends Actor
  with Json4sJacksonSupport
  with SLF4JLogging
  with SparktaSerializer {

  override def receive: Receive = {
    case FindByTypeAndId(fragmentType, id) => findByTypeAndId(fragmentType, id)
    case FindByTypeAndName(fragmentType, name) => findByTypeAndName(fragmentType, name.toLowerCase())
    case Create(fragment) => create(fragment)
    case Update(fragment) => update(fragment)
    case DeleteByTypeAndId(fragmentType, id) => deleteByTypeAndId(fragmentType, id)
    case FindByType(fragmentType) => findByType(fragmentType)
  }

  def findByType(fragmentType: String): Unit =
    sender ! ResponseFragments(Try({
      val children = curatorFramework.getChildren.forPath(FragmentActor.fragmentPath(fragmentType))
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        read[FragmentElementModel](new String(curatorFramework.getData.forPath(
          s"${FragmentActor.fragmentPath(fragmentType)}/$element")))).toSeq
    }).recover {
      case e: NoNodeException => Seq()
    })

  def findByTypeAndId(fragmentType: String, id: String): Unit =
    sender ! new ResponseFragment(Try({
      log.info(s"> Retrieving information for path: ${FragmentActor.fragmentPath(fragmentType)}/$id)")
      read[FragmentElementModel](new String(curatorFramework.getData.forPath(
        s"${FragmentActor.fragmentPath(fragmentType)}/$id")))
    }).recover {
      case e: NoNodeException => throw new ServingApiException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId, s"No fragment of type ${fragmentType} with id ${id}.")
      ))
    })

  def findByTypeAndName(fragmentType: String, name: String): Unit =
    sender ! ResponseFragment(Try({
      val children = curatorFramework.getChildren.forPath(FragmentActor.fragmentPath(fragmentType))
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        read[FragmentElementModel](new String(curatorFramework.getData.forPath(
          s"${FragmentActor.fragmentPath(fragmentType)}/$element"))))
        .filter(fragment => fragment.name == name).head
    }).recover {
      case e: NoNodeException => throw new ServingApiException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsFragmentWithName,
          s"No fragment of type ${fragmentType} with name ${name}.")
      ))
      case e: NoSuchElementException => throw new ServingApiException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsPolicytWithName,
          s"No fragment of type ${fragmentType} with name ${name}")
      ))
    })

  def create(fragment: FragmentElementModel): Unit =
    sender ! ResponseFragment(Try({
      if(existsByTypeAndName(fragment.fragmentType, fragment.name.toLowerCase)) {
        throw new ServingApiException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeExistsFragmentWithName,
            s"Fragment of type ${fragment.fragmentType} with name ${fragment.name} exists.")
        ))
      }

      val currentId: Option[String] = if(fragment.id.isDefined) fragment.id else Option(UUID.randomUUID.toString)
      val fragmentS = fragment.copy(id = currentId,
        name = fragment.name.toLowerCase)
      curatorFramework.create().creatingParentsIfNeeded().forPath(
        s"${FragmentActor.fragmentPath(
          fragmentS.fragmentType)}/${fragmentS.id.get}", write(fragmentS).getBytes())
      fragmentS
    }))

  def update(fragment: FragmentElementModel): Unit =
    sender ! Response(Try({
      if(existsByTypeAndName(fragment.fragmentType, fragment.name.toLowerCase, fragment.id)) {
        throw new ServingApiException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeExistsFragmentWithName,
            s"Fragment of type ${fragment.fragmentType} with name ${fragment.name} exists.")
        ))
      }

      val fragmentS = fragment.copy(name = fragment.name.toLowerCase)

      curatorFramework.setData.forPath(
        s"${FragmentActor.fragmentPath(fragmentS.fragmentType)}/${fragment.id.get}", write(fragmentS).getBytes)
    }).recover {
      case e: NoNodeException => throw new ServingApiException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId,
          s"No fragment of type ${fragment.fragmentType} with id ${fragment.id.get}.")
      ))
    })

  def deleteByTypeAndId(fragmentType: String, id: String): Unit =
    sender ! Response(Try({
      curatorFramework.delete().forPath(s"${FragmentActor.fragmentPath(fragmentType)}/$id")
    }).recover {
      case e: NoNodeException => throw new ServingApiException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId, s"No fragment of type ${fragmentType} with id ${id}.")
      ))
    })

  private def existsByTypeAndName(fragmentType: String, name: String, id: Option[String] = None): Boolean = {
    Try({
      val fragmentLocation = fragmentPath(fragmentType)
      if(Option(curatorFramework.checkExists().forPath(fragmentLocation)).isDefined) {
        val children = curatorFramework.getChildren.forPath(fragmentLocation)
        JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[FragmentElementModel](new String(curatorFramework.getData.forPath(s"$fragmentLocation/$element"))))
          .filter(fragment => {
          if (id.isDefined) fragment.name == name && fragment.id.get != id.get
          else fragment.name == name
        }).toSeq.nonEmpty
      } else false
    }) match {
      case Success(result) => result
      case Failure(exception) => {
        log.error(exception.getLocalizedMessage, exception)
        false
      }
    }
  }
}

object FragmentActor {

  case class Create(fragment: FragmentElementModel)

  case class Update(fragment: FragmentElementModel)

  case class FindByType(fragmentType: String)

  case class FindByTypeAndId(fragmentType: String, id: String)

  case class FindByTypeAndName(fragmentType: String, name: String)

  case class DeleteByTypeAndId(fragmentType: String, id: String)

  case class ResponseFragment(fragment: Try[FragmentElementModel])

  case class ResponseFragments(fragments: Try[Seq[FragmentElementModel]])

  case class Response(status: Try[_])

  def fragmentPath(fragmentType: String): String = {
    fragmentType match {
      case "input" => s"${AppConstant.BaseZKPath}/fragments/input"
      case "output" => s"${AppConstant.BaseZKPath}/fragments/output"
      case _ => throw new IllegalArgumentException("The fragment type must be input|output")
    }
  }
}