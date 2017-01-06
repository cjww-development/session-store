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
package services

import models.InitialSession
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, OFormat}
import reactivemongo.api.commands.UpdateWriteResult
import repositories.SessionRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SessionService extends SessionService {
  val sessionRepo = SessionRepository
}

trait SessionService {

  val sessionRepo : SessionRepository

  def cacheData(sessionID : String, data : String) : Future[Boolean] = {
    sessionRepo.cacheData(sessionID, data) map {
      wr =>
        if(wr.hasErrors) {
          // $COVERAGE-OFF$
          Logger.error(s"[SessionRepo] - [cacheData] : There was a problem caching the data - ${wr.errmsg}")
          // $COVERAGE-ON$
        }
        wr.hasErrors
    }
  }

  def getByKey(sessionID : String, key : String)(implicit format : OFormat[InitialSession]) : Future[Option[String]] = {
    sessionRepo.getData(sessionID, key) map {
      data =>
        if(data.isEmpty) {
          // $COVERAGE-OFF$
          Logger.error(s"[SessionRepo] - [getByKey] : data for this key could not be found")
          // $COVERAGE-ON$
        }
        data
    }
  }

  def updateDataKey(sessionID : String, key : String, data : String)(implicit format : OFormat[InitialSession]) : Future[UpdateWriteResult] = {
    for {
      Some(session) <- sessionRepo.getSession(sessionID)
      uwr <- sessionRepo.updateSession(sessionID, session, key, data)
    } yield {
      uwr
    }
  }

  def destroySessionRecord(sessionID : String) : Future[Boolean] = {
    sessionRepo.removeSessionRecord(sessionID) map {
      wr =>
        if(wr.hasErrors) {
          // $COVERAGE-OFF$
          Logger.error(s"[SessionRepo] - [destroySessionRecord] : There was a problem deleting the session - ${wr.errmsg}")
          // $COVERAGE-ON$
        }
        wr.hasErrors
    }
  }
}
