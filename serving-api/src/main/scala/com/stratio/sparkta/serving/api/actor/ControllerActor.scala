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

import akka.actor.{ActorContext, _}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.serving.api.constants.AkkaConstant
import com.stratio.sparkta.serving.api.service.http._
import com.stratio.sparkta.serving.core.models.SparktaSerializer
import spray.routing._
import com.stratio.sparkta.serving.api.service.handler.CustomExceptionHandler._


class ControllerActor(actorsMap: Map[String, ActorRef]) extends HttpServiceActor
with SLF4JLogging
with SparktaSerializer {

  override implicit def actorRefFactory: ActorContext = context

  val serviceRoutes: ServiceRoutes = new ServiceRoutes(actorsMap, context)

  def receive: Receive = runRoute(handleExceptions(exceptionHandler)(getRoutes))

  def getRoutes: Route = webRoutes ~ serviceRoutes.fragmentRoute ~
    serviceRoutes.policyContextRoute ~ serviceRoutes.policyRoute ~
    serviceRoutes.templateRoute ~ serviceRoutes.AppStatusRoute

  def webRoutes: Route =
    get {
      pathPrefix("") {
        pathEndOrSingleSlash {
          getFromResource("classes/web/index.html")
        }
      } ~ getFromResourceDirectory("classes/web") ~
        pathPrefix("") {
          pathEndOrSingleSlash {
            getFromResource("web/index.html")
          }
        } ~ getFromResourceDirectory("web")
    }
}

class ServiceRoutes(actorsMap: Map[String, ActorRef], context: ActorContext) {

  val fragmentRoute: Route = new FragmentHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap.get(AkkaConstant.FragmentActor).get
    override val actorRefFactory: ActorRefFactory = context
  }.routes

  val templateRoute: Route = new TemplateHttpService {
    implicit val actors = actorsMap
    override val supervisor =actorsMap.get(AkkaConstant.TemplateActor).get
    override val actorRefFactory: ActorRefFactory = context
  }.routes

  val policyRoute: Route = new PolicyHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap.get(AkkaConstant.PolicyActor).get
    override val actorRefFactory: ActorRefFactory = context
  }.routes

  val policyContextRoute: Route = new PolicyContextHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap.get(AkkaConstant.SparkStreamingContextActor).get
    override val actorRefFactory: ActorRefFactory = context
  }.routes

  val AppStatusRoute: Route = new AppStatusHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = context.self
    override val actorRefFactory: ActorRefFactory = context
  }.routes
}