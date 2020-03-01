/*
 * Copyright 2020 CJWW Development
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
 */

package controllers

import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.featuremanagement.services.FeatureService
import com.cjwwdev.mongo.responses.MongoSuccessUpdate
import common.BackendController
import javax.inject.Inject
import play.api.libs.json.{JsString, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import reactivemongo.core.actors.Exceptions.PrimaryUnavailableException
import reactivemongo.core.errors.DatabaseException
import repositories.SessionRepository
import services.SessionService

import scala.concurrent.ExecutionContext

class DefaultSessionController @Inject()(val sessionService: SessionService,
                                         val sessionRepository: SessionRepository,
                                         val config: ConfigurationLoader,
                                         val controllerComponents: ControllerComponents,
                                         val featureService: FeatureService,
                                         implicit val ec: ExecutionContext) extends SessionController {
  override val appId: String = config.getServiceId(config.get[String]("appName"))
}

trait SessionController extends BackendController {

  val sessionService: SessionService

  def initialiseSession(sessionId: String): Action[String] = Action.async(parse.text) { implicit request =>
    applicationVerification {
      validateAs(SESSION, sessionId) {
        sessionService.cacheData(sessionId) map { session =>
          if(session.isDefined) {
            withJsonResponseBody(CREATED, Json.toJson(session.get)) { json =>
              Created(json)
            }
          } else {
            withJsonResponseBody(INTERNAL_SERVER_ERROR, s"There was a problem caching the session data for session $sessionId") { json =>
              InternalServerError(json)
            }
          }
        } recover {
          case e: DatabaseException if e.code.contains(DUPLICATE_ERR_CODE) =>
            withJsonResponseBody(BAD_REQUEST, s"A session already exists against sessionId $sessionId") { json =>
              BadRequest(json)
            }
          case _: PrimaryUnavailableException =>
            withJsonResponseBody(INTERNAL_SERVER_ERROR, s"There was a problem caching the session data for session $sessionId") { json =>
              InternalServerError(json)
            }
        }
      }
    }
  }

  def getEntry(sessionId: String, key: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    applicationVerification {
      validateSession(sessionId) { session =>
        key match {
          case Some(dataKey) => sessionService.getByKey(session.sessionId, dataKey) map { data =>
            val (status, body) = data match {
              case Left(value) => value.fold(NO_CONTENT -> s"No data found for session key $dataKey")(OK -> _)
              case Right(_)    => NOT_FOUND -> s"No session found for sessionId $sessionId"
            }

            withJsonResponseBody(status, body) {
              Status(status)(_)
            }
          }
          case None => sessionService.getSession(sessionId) map { session =>
            val (status, body) = session.fold[(Int, JsValue)]((NOT_FOUND, JsString(s"No session found for sessionId $sessionId")))(
              session => (OK, Json.toJson(session))
            )
            withJsonResponseBody(status, body) {
              Status(status)(_)
            }
          }
        }
      }
    }
  }

  def updateSession(sessionId: String): Action[String] = Action.async(parse.text) { implicit request =>
    applicationVerification {
      validateSession(sessionId) { session =>
        val updateData = Json.parse(request.body).as[Map[String, String]](mapReads)
        sessionService.updateDataKey(session.sessionId, updateData) map { resp =>
          val noFailures = resp.forall { case (_, r) => r.equals(MongoSuccessUpdate.toString) }
          val respToStringVal = resp.map { case (e, r) => if(r.equals(MongoSuccessUpdate.toString)) (e, "Updated") else (e, "Problem updating") }
          val status = if(noFailures) OK else INTERNAL_SERVER_ERROR
          withJsonResponseBody(status, Json.toJson(respToStringVal.toMap)) {
            Status(status)(_)
          }
        }
      }
    }
  }

  def destroy(sessionId: String): Action[AnyContent] = Action.async { implicit request =>
    applicationVerification {
      validateSession(sessionId) { session =>
        sessionService.destroySessionRecord(session.sessionId) map { destroyed =>
          val (status, body) = if(destroyed) {
            (NO_CONTENT, "The session has been deleted")
          } else {
            (INTERNAL_SERVER_ERROR, "There was problem deleting the specified session")
          }

          withJsonResponseBody(status, body) {
            Status(status)(_)
          }
        }
      }
    }
  }
}
