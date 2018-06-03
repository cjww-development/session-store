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

package common

import com.cjwwdev.auth.backend.BaseAuth
import com.cjwwdev.http.headers.HttpHeaders
import com.cjwwdev.identifiers.IdentifierValidation
import com.cjwwdev.request.RequestParsers
import com.cjwwdev.responses.ApiResponse
import models.Session
import org.joda.time.{DateTime, Interval}
import play.api.libs.json._
import play.api.mvc._
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BackendController extends BaseController with RequestParsers with IdentifierValidation with BaseAuth with HttpHeaders with ApiResponse {
  val sessionRepository: SessionRepository

  protected def validateSession(id: String)(continue: Session => Future[Result])(implicit format: OFormat[Session], request: Request[_]): Future[Result] = {
    validateAs(SESSION, id) {
      sessionRepository.validateSession(id) flatMap { if(_) {
       sessionRepository.getSession(id) flatMap(_.fold(logWithForbidden(id, "[validateSession] - Session is invalid: action forbidden"))(
         session => if(validateTimestamps(session.modifiedDetails.lastModified)) continue(session) else destroySession(id)
       ))
      } else {
        logWithForbidden(id, "[validateSession] - Session doesn't exist, action forbidden")
      }}
    }
  }

  private def logWithForbidden(sessionId: String, msg: String)(implicit request: Request[_]): Future[Result] = {
    logger.warn(msg)
    withFutureJsonResponseBody(FORBIDDEN, msg) { json =>
      Future(Forbidden(json))
    }
  }

  private def destroySession(sessionId: String)(implicit request: Request[_]): Future[Result] = {
    logger.warn("[validateSession]: Session has timed out, action forbidden")
    sessionRepository.removeSession(sessionId).flatMap { _ =>
      withFutureJsonResponseBody(FORBIDDEN, s"Session $sessionId has timed out action forbidden") { json =>
        Future(Forbidden(json))
      }
    }
  }

  private val validateTimestamps: DateTime => Boolean = lastModified => !(new Interval(lastModified, DateTime.now).toDuration.getStandardHours >= 1)

  val NoContentWithBody: JsValue => Result = msg => new Status(NO_CONTENT)(msg)

  val mapReads: Reads[Map[String, String]] = Reads[Map[String, String]](json => JsSuccess(json.as[Map[String, String]]))

  val DUPLICATE_ERR_CODE = 11000
}
