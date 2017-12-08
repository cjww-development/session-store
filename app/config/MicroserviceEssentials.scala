// Copyright (C) 2016-2017 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package config

import javax.inject.Inject

import com.cjwwdev.auth.actions.BaseAuth
import com.cjwwdev.filters.RequestLoggingFilter
import com.cjwwdev.identifiers.IdentifierValidation
import com.cjwwdev.request.RequestParsers
import com.kenshoo.play.metrics.MetricsFilter
import models.Session
import org.joda.time.{DateTime, Interval}
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.DefaultHttpFilters
import play.api.libs.json.OFormat
import play.api.mvc.{Controller, Request, Result}
import repositories.SessionRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait BackController extends Controller with RequestParsers with IdentifierValidation with BaseAuth {

  val sessionRepository: SessionRepository

  val logger: Logger = LoggerFactory.getLogger(getClass)

  protected def validateSession(id: String)(continue: Session => Future[Result])(implicit format: OFormat[Session], request: Request[_]): Future[Result] = {
    validateAs(SESSION, id) {
      matchUrlAndHeader(id) {
        sessionRepository.validateSession(id) flatMap { if(_) {
         sessionRepository.getSession(id) flatMap(_.fold(logWithForbidden("[validateSession] - Session is invalid: action forbidden"))(
           session => if(validateTimestamps(session.modifiedDetails.lastModified)) continue(session) else destroySession(id)
         ))
        } else {
          logWithForbidden("[validateSession] - Session doesn't exist, action forbidden")
        }}
      }
    }
  }

  private def matchUrlAndHeader(sessionId: String)(continue: => Future[Result])(implicit request: Request[_]): Future[Result] = {
    val matchingIds: String => Future[Result] = cookieId =>
      if(cookieId == sessionId) continue else logWithForbidden(s"[matchUrlAndHeader] - header and url mismatch ($cookieId != $sessionId)")
    request.headers.get("cookieId").fold(logWithForbidden("[matchUrlAndHeader] - No cookieId found in the request header"))(matchingIds)
  }

  private def logWithForbidden(msg: String): Future[Result] = {
    logger.warn(msg)
    Future.successful(Forbidden)
  }

  private def destroySession(sessionId: String)(implicit request: Request[_]): Future[Result] = {
    logger.warn("[validateSession]: Session has timed out, action forbidden")
    sessionRepository.removeSession(sessionId) map(_ => Forbidden)
  }

  private val validateTimestamps: DateTime => Boolean = lastModified => !(new Interval(lastModified, DateTime.now).toDuration.getStandardHours >= 1)
}

class EnabledFilters @Inject()(loggingFilter: RequestLoggingFilter, metricsFilter: MetricsFilter)
  extends DefaultHttpFilters(loggingFilter, metricsFilter)

class SessionKeyNotFoundException(msg: String) extends Exception(msg)
class MissingSessionException(msg: String) extends Exception(msg)
