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

package com.stratio.sparkta.serving.api.service.handler

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.serving.api.exception.ServingApiException
import com.stratio.sparkta.serving.core.models.{ErrorModel, SparktaSerializer}
import org.json4s.jackson.Serialization._
import spray.http.StatusCodes
import spray.routing.ExceptionHandler
import spray.routing.directives.{MiscDirectives, RouteDirectives}
import spray.util.LoggingContext

/**
 * This exception handler will be used by all our services to return a [ErrorModel] that will be used by the frontend.
 */
object CustomExceptionHandler extends MiscDirectives
with RouteDirectives
with SLF4JLogging
with SparktaSerializer {

  implicit def exceptionHandler(implicit logg: LoggingContext): ExceptionHandler = {
    ExceptionHandler {
      case exception: ServingApiException =>
        requestUri { uri =>
          log.error(exception.getLocalizedMessage)
          complete((StatusCodes.NotFound, write(ErrorModel.toErrorModel(exception.getLocalizedMessage))))
        }
      case exception: Throwable =>
        requestUri { uri =>
          log.error(exception.getLocalizedMessage, exception)
          complete((StatusCodes.InternalServerError, write(
            new ErrorModel(ErrorModel.CodeUnknow, Option(exception.getLocalizedMessage).getOrElse("unknown"))
          )))
        }
    }
  }
}