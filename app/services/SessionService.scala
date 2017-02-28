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

package services

import javax.inject.{Inject, Singleton}

import com.cjwwdev.mongo._
import com.cjwwdev.logging.Logger
import models.InitialSession
import play.api.libs.json.OFormat
import repositories.SessionRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SessionService @Inject()(sessionRepo: SessionRepository) {
  def cacheData(sessionID: String, data: String): Future[Boolean] = {
    sessionRepo.cacheData(sessionID, data) map {
      case MongoSuccessCreate => true
      case MongoFailedCreate  =>
        Logger.error(s"[SessionRepo] - [cacheData] : There was a problem caching the data")
        false
      case _ => throw new IllegalStateException()
    }
  }

  def getByKey(sessionID : String, key : String)(implicit format : OFormat[InitialSession]) : Future[Option[String]] = {
    sessionRepo.getData(sessionID, key) map {
      data =>
        if(data.isEmpty) Logger.error(s"[SessionRepo] - [getByKey] : data for this key could not be found")
        data
    }
  }

  def updateDataKey(sessionID : String, key : String, data : String)(implicit format : OFormat[InitialSession]) : Future[MongoResponse] = {
    for {
      Some(session) <- sessionRepo.getSession(sessionID)
      mongoResponse <- sessionRepo.updateSession(sessionID, session, key, data)
    } yield {
      mongoResponse
    }
  }

  def destroySessionRecord(sessionID : String) : Future[Boolean] = {
    sessionRepo.removeSessionRecord(sessionID) map {
      case MongoSuccessDelete => true
      case MongoFailedDelete =>
        Logger.error(s"[SessionRepo] - [destroySessionRecord] : There was a problem deleting the session")
        false
      case _ => throw new IllegalStateException()
    }
  }
}
