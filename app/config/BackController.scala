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

import com.cjwwdev.logging.Logger
import com.cjwwdev.security.encryption.DataSecurity
import models.Session
import org.joda.time.{DateTime, Interval}
import play.api.libs.json._
import play.api.mvc.{Controller, Request, Result}
import repositories.SessionRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

trait BackController extends Controller {

  val sessionRepo = new SessionRepository

  protected def processJsonBody[T](f: (T) => Future[Result])(implicit request : JsValue, manifest : Manifest[T], reads : Reads[T]): Future[Result] =
    Try(request.validate[T]) match {
      case Success(JsSuccess(data, _)) => f(data)
      case Success(JsError(_)) => Future.successful(BadRequest)
      case Failure(_) => Future.successful(BadRequest)
    }

  protected def decryptRequest[T](f: (T) => Future[Result])
                                 (implicit request: Request[String], manifest : Manifest[T], reads : Reads[T], format : Format[T]): Future[Result] = {
    Try(DataSecurity.decryptInto[T](request.body)) match {
      case Success(Some(data)) =>
        Logger.info("[BackController] - [decryptRequest] Request body decryption successful")
        f(data)
      case Success(None) => Future.successful(BadRequest)
      case Failure(e) =>
        Logger.error(s"[BackController] - [decryptRequest] Request body decryption FAILED - reason : ${e.getStackTrace}")
        Future.successful(BadRequest)
    }
  }

  protected def validateSession(id: String)(f: Session => Future[Result])(implicit format: OFormat[Session]): Future[Result] = {
    sessionRepo.store.getSession(id) flatMap {
      case Some(session) => if(validateTimestamps(session.modifiedDetails.lastModified)) f(session) else Future.successful(Forbidden)
      case None =>
        Logger.warn("[BackController] - [validateSession]: Session is invalid, action forbidden")
        Future.successful(Forbidden)
    }
  }

  private def validateTimestamps(lastModified: DateTime): Boolean = {
    val interval = new Interval(lastModified, DateTime.now)
    Logger.debug(s"TIME INTERVAL IS ${interval.toDuration.getStandardHours}")
    if(interval.toDuration.getStandardHours >= 1) false else true
  }
}
