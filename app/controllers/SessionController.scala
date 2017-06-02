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

import com.cjwwdev.auth.actions.{Authorised, BaseAuth, NotAuthorised}
import com.cjwwdev.reactivemongo.{MongoFailedUpdate, MongoSuccessUpdate}
import play.api.mvc.{Action, AnyContent}
import config.{BackController, MissingSessionException, SessionKeyNotFoundException}
import models.UpdateSet
import services.SessionService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SessionController @Inject()(sessionService: SessionService) extends BackController with BaseAuth {

  def cache(sessionId: String) : Action[String] = Action.async(parse.text) {
    implicit request =>
      openActionVerification {
        case Authorised =>
          sessionService.cacheData(sessionId, request.body) map { cached =>
            if(cached) Created else InternalServerError
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def getEntry(sessionId: String, key: String) : Action[AnyContent] = Action.async {
    implicit request =>
      openActionVerification {
        case Authorised =>
          validateSession(sessionId) { session =>
            sessionService.getByKey(session.sessionId, key) map {
              data => Ok(data)
            } recover {
              case _: SessionKeyNotFoundException => NotFound
              case _: MissingSessionException     => Forbidden
            }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def updateSession(sessionId: String) : Action[String] = Action.async(parse.text) {
    implicit request =>
      openActionVerification {
        case Authorised =>
        validateSession(sessionId) { session =>
            decryptRequest[UpdateSet](UpdateSet.standardFormat) { updateData =>
              sessionService.updateDataKey(session.sessionId, updateData) map {
                case MongoSuccessUpdate => Ok
                case MongoFailedUpdate => InternalServerError
              }
            }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def destroy(sessionId: String) : Action[AnyContent] = Action.async {
    implicit request =>
      openActionVerification {
        case Authorised =>
          validateSession(sessionId) { session =>
            sessionService.destroySessionRecord(session.sessionId) map { destroyed =>
              if(destroyed) Ok else InternalServerError
            }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }
}
