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

package controllers

import javax.inject.{Inject, Singleton}

import com.cjwwdev.mongo.{MongoFailedUpdate, MongoSuccessUpdate}
import config.{Authorised, BackController, NotAuthorised}
import models.UpdateSet
import com.cjwwdev.logging.Logger
import play.api.mvc.Action
import services.SessionService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SessionController @Inject()(sessionService: SessionService) extends BackController {

  def cache(sessionId: String) : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          sessionService.cacheData(sessionId, request.body) map {
            case true => Created
            case false => InternalServerError
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def getEntry(sessionId: String, key: String) : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          validateSession(sessionId) { session =>
            Logger.info(s"[SessionController] - [getEntry] key = $key")
            sessionService.getByKey(session.sessionId, key) map {
              case Some(data) => Ok(data)
              case None => NotFound
            }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def updateSession(sessionId: String) : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          validateSession(sessionId) { session =>
            decryptRequest[UpdateSet] { updateData =>
              sessionService.updateDataKey(session.sessionId, updateData.key, updateData.data) map {
                case MongoSuccessUpdate => Ok
                case _ => InternalServerError
              }
            }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def destroy(sessionId: String) : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          validateSession(sessionId) { session =>
            sessionService.destroySessionRecord(session.sessionId) map {
              case true => Ok
              case false => InternalServerError
            }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }
}
