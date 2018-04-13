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

import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.mongo.responses.MongoSuccessUpdate
import common.{BackController, MissingSessionException}
import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import repositories.SessionRepository
import services.SessionService

import scala.concurrent.ExecutionContext.Implicits.global

class SessionControllerImpl @Inject()(val sessionService: SessionService,
                                      val sessionRepository: SessionRepository,
                                      val configurationLoader: ConfigurationLoader) extends SessionController

trait SessionController extends BackController {

  val sessionService: SessionService

  def initialiseSession(sessionId: String): Action[String] = Action.async(parse.text) { implicit request =>
    applicationVerification {
      validateAs(SESSION, sessionId) {
        sessionService.cacheData(sessionId) map { cached =>
          if(cached) Created else InternalServerError(s"There was a problem caching the session data for session $sessionId")
        }
      }
    }
  }

  def getEntry(sessionId: String, key: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    applicationVerification {
      validateSession(sessionId) { session =>
        key match {
          case Some(dataKey) => sessionService.getByKey(session.sessionId, dataKey) map {
            _.fold(NoContentWithBody(s"No data found for session key $key under session $sessionId"))(Ok(_))
          } recover {
            case _: MissingSessionException => NotFound(s"No session found for session $sessionId")
          }
          case None => sessionService.getSession(sessionId) map {
            _.fold(NotFound(s"No session found for session $sessionId"))(session => Ok(Json.toJson(session)))
          }
        }
      }
    }
  }

  def updateSession(sessionId: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    applicationVerification {
      validateSession(sessionId) { session =>
        val updateData = request.body.as[Map[String, String]](mapReads)
        sessionService.updateDataKey(session.sessionId, updateData) map { resp =>
          val noFailures = resp.forall{ case (_, r) => r.equals(MongoSuccessUpdate.toString)}
          if(noFailures) {
            Ok(Json.toJson(resp.toMap))
          } else {
            InternalServerError(Json.toJson(resp.toMap))
          }
        }
      }
    }
  }

  def destroy(sessionId: String): Action[AnyContent] = Action.async { implicit request =>
    applicationVerification {
      validateSession(sessionId) { session =>
        sessionService.destroySessionRecord(session.sessionId) map { destroyed =>
          if (destroyed) NoContent else InternalServerError(s"There was a problem destroying the session $sessionId")
        }
      }
    }
  }
}
