/*
 * Copyright 2018 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package controllers

import javax.inject.Inject

import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.reactivemongo.{MongoFailedUpdate, MongoSuccessUpdate}
import com.cjwwdev.security.encryption.DataSecurity
import config.{BackController, SessionKeyNotFoundException}
import models.UpdateSet
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import repositories.SessionRepository
import services.SessionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionControllerImpl @Inject()(val sessionService: SessionService,
                                      val sessionRepository: SessionRepository,
                                      val configurationLoader: ConfigurationLoader) extends SessionController

trait SessionController extends BackController {

  val sessionService: SessionService

  def cache(sessionId: String): Action[String] = Action.async(parse.text) { implicit request =>
    openActionVerification {
      validateAs(SESSION, sessionId) {
        sessionService.cacheData(sessionId, request.body) map { cached =>
          if (cached) Created else InternalServerError
        }
      }
    }
  }

  def getEntry(sessionId: String, key: String): Action[AnyContent] = Action.async { implicit request =>
    openActionVerification {
      validateSession(sessionId) { session =>
        sessionService.getByKey(session.sessionId, key) map { data =>
          Ok(data)
        } recover {
          case _: SessionKeyNotFoundException => NotFound
        }
      }
    }
  }

  def updateSession(sessionId: String): Action[String] = Action.async(parse.text) { implicit request =>
    openActionVerification {
      validateSession(sessionId) { session =>
        withJsonBody[UpdateSet](UpdateSet.standardFormat) { updateData =>
          sessionService.updateDataKey(session.sessionId, updateData) map {
            case MongoSuccessUpdate => Ok
            case MongoFailedUpdate  => InternalServerError
          }
        }
      }
    }
  }

  def destroy(sessionId: String): Action[AnyContent] = Action.async { implicit request =>
    openActionVerification {
      validateSession(sessionId) { session =>
        sessionService.destroySessionRecord(session.sessionId) map { destroyed =>
          if (destroyed) Ok else InternalServerError
        }
      }
    }
  }

  def getContextId(sessionId: String): Action[AnyContent] = Action.async { implicit request =>
    openActionVerification {
      validateSession(sessionId) { session =>
        val contextId = DataSecurity.encryptType[JsValue](Json.parse(s"""{"contextId" : "${session.data("contextId")}"}"""))
        Future.successful(Ok(contextId))
      }
    }
  }
}
