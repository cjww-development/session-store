// Copyright (C) 2011-2012 the original author or authors.
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

import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Controller, Request, Result}
import security.JsonSecurity

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

sealed trait AuthorisationResponse
case object NotAuthorised extends AuthorisationResponse
case object Authorised extends AuthorisationResponse

trait BackController extends Controller with ConfigurationStrings {

  protected def processJsonBody[T](f: (T) => Future[Result])(implicit request : JsValue, manifest : Manifest[T], reads : Reads[T]) =
    Try(request.validate[T]) match {
      case Success(JsSuccess(data, _)) => f(data)
      case Success(JsError(errs)) => Future.successful(BadRequest)
      case Failure(e) => Future.successful(BadRequest)
    }

  protected def decryptRequest[T](f: (T) => Future[Result])(implicit request: Request[String], manifest : Manifest[T], reads : Reads[T], format : Format[T]) = {
    Try(JsonSecurity.decryptInto[T](request.body)) match {
      case Success(Some(data)) => f(data)
      case Success(None) =>
        Logger.debug(s"[BackController] [decryptRequest] Request body not found : ${request.body}")
        Future.successful(BadRequest)
      case Failure(e) => Future.successful(BadRequest)
    }
  }

  protected def authOpenAction(f: (AuthorisationResponse) => Future[Result])(implicit request: Request[_]) = {
    f(checkAuth(request.headers.get("appID")))
  }

  private def checkAuth(appID : Option[String]) : AuthorisationResponse = {
    appID match {
      case Some(id) => id match {
        case AUTH_ID | DIAG_ID | DEV_ID => Authorised
        case _ => NotAuthorised
      }
      case None => NotAuthorised
    }
  }
}
