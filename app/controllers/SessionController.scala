/*
 *  Copyright 2018 CJWW Development
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package controllers

import javax.inject.Inject

import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.mongo.responses.{MongoFailedUpdate, MongoSuccessUpdate}
import com.cjwwdev.security.encryption.DataSecurity
import common.{BackController, MissingSessionException, SessionKeyNotFoundException}
import models.UpdateSet
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
    applicationVerification {
      validateAs(SESSION, sessionId) {
        sessionService.cacheData(sessionId, request.body) map { cached =>
          if(cached) Created else InternalServerError(s"There was a problem caching the session data for session $sessionId")
        }
      }
    }
  }

  def getEntry(sessionId: String, key: String): Action[AnyContent] = Action.async { implicit request =>
    applicationVerification {
      validateSession(sessionId) { session =>
        sessionService.getByKey(session.sessionId, key) map { data =>
          Ok(data)
        } recover {
          case _: SessionKeyNotFoundException => NoContentWithBody(s"No data found for session key $key under session $sessionId")
          case _: MissingSessionException     => NotFound(s"No session found for session $sessionId")
        }
      }
    }
  }

  def updateSession(sessionId: String): Action[String] = Action.async(parse.text) { implicit request =>
    applicationVerification {
      validateSession(sessionId) { session =>
        withJsonBody[UpdateSet](UpdateSet.standardFormat) { updateData =>
          sessionService.updateDataKey(session.sessionId, updateData) map {
            case MongoSuccessUpdate => Created
            case MongoFailedUpdate  => InternalServerError(s"There was a problem updating the session data for session $sessionId")
          }
        }
      }
    }
  }

  def destroy(sessionId: String): Action[AnyContent] = Action.async { implicit request =>
    applicationVerification {
      validateSession(sessionId) { session =>
        sessionService.destroySessionRecord(session.sessionId) map { destroyed =>
          if (destroyed) Ok else InternalServerError(s"There was a problem destroying the session $sessionId")
        }
      }
    }
  }

  def getContextId(sessionId: String): Action[AnyContent] = Action.async { implicit request =>
    applicationVerification {
      validateSession(sessionId) { session =>
        Future.successful(Ok(session.data("contextId")))
      }
    }
  }
}
