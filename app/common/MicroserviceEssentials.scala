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

import javax.inject.Inject
import com.cjwwdev.auth.backend.BaseAuth
import com.cjwwdev.filters.RequestLoggingFilter
import com.cjwwdev.http.headers.HttpHeaders
import com.cjwwdev.identifiers.IdentifierValidation
import com.cjwwdev.implicits.ImplicitHandlers
import com.cjwwdev.mongo.indexes.RepositoryIndexer
import com.cjwwdev.request.RequestParsers
import com.kenshoo.play.metrics.MetricsFilter
import models.Session
import org.joda.time.{DateTime, Interval}
import play.api.http.DefaultHttpFilters
import play.api.libs.json._
import play.api.mvc.{Controller, Request, Result}
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BackController extends Controller with RequestParsers with IdentifierValidation with BaseAuth with HttpHeaders with ImplicitHandlers {
  val sessionRepository: SessionRepository

  protected def validateSession(id: String)(continue: Session => Future[Result])(implicit format: OFormat[Session], request: Request[_]): Future[Result] = {
    validateAs(SESSION, id) {
      sessionRepository.validateSession(id) flatMap { if(_) {
       sessionRepository.getSession(id) flatMap(_.fold(logWithForbidden("[validateSession] - Session is invalid: action forbidden"))(
         session => if(validateTimestamps(session.modifiedDetails.lastModified)) continue(session) else destroySession(id)
       ))
      } else {
        logWithForbidden("[validateSession] - Session doesn't exist, action forbidden")
      }}
    }
  }

  private def logWithForbidden(msg: String): Future[Result] = {
    logger.warn(msg)
    Future.successful(Forbidden)
  }

  private def destroySession(sessionId: String)(implicit request: Request[_]): Future[Result] = {
    logger.warn("[validateSession]: Session has timed out, action forbidden")
    sessionRepository.removeSession(sessionId) map(_ => Forbidden(s"Session $sessionId has timed out action forbidden"))
  }

  private val validateTimestamps: DateTime => Boolean = lastModified => !(new Interval(lastModified, DateTime.now).toDuration.getStandardHours >= 1)

  val NoContentWithBody: String => Result = msg => new Status(NO_CONTENT)(msg)

  val mapReads: Reads[Map[String, String]] = new Reads[Map[String, String]] {
    override def reads(json: JsValue): JsResult[Map[String, String]] = {
      JsSuccess(json.as[Map[String, String]])
    }
  }
}

class EnabledFilters @Inject()(loggingFilter: RequestLoggingFilter, metricsFilter: MetricsFilter)
  extends DefaultHttpFilters(loggingFilter, metricsFilter)

class RepositoryIndexerImpl @Inject()(sessionRepository: SessionRepository) extends RepositoryIndexer {
  override val repositories = Seq(sessionRepository)
  runIndexing
}

class SessionKeyNotFoundException(msg: String) extends Exception(msg)
class MissingSessionException(msg: String) extends Exception(msg)
