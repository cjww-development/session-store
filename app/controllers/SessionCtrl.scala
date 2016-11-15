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

package controllers

import config.{Authorised, BackController, NotAuthorised}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Action
import services.SessionService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait SessionCtrl extends BackController {

  val sessionService : SessionService

  def cache : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          sessionService.cacheData(request.headers("sessionID"), request.body) map {
            case true => InternalServerError
            case false => Created
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def getEntry : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[String] {
            key =>
              Logger.info(s"[SessionController] - [getEntry] key = $key")
              sessionService.getByKey(request.headers("sessionID"), key) map {
                case Some(data) => Ok(data)
                case None => NotFound
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def destroy : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[String] {
            sessionId =>
              sessionService.destroySessionRecord(sessionId) map {
                case true => InternalServerError
                case false => Ok
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }
}