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
 */

package services

import javax.inject.Inject

import com.cjwwdev.reactivemongo._
import com.cjwwdev.security.encryption.DataSecurity
import config.{MissingSessionException, SessionKeyNotFoundException}
import models.{Session, UpdateSet}
import play.api.libs.json.{JsValue, Json, OFormat}
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionServiceImpl @Inject()(val sessionRepo: SessionRepository) extends SessionService

trait SessionService {
  val sessionRepo: SessionRepository

  def cacheData(sessionId: String, contextId: String): Future[Boolean] = {
    sessionRepo.cacheData(sessionId, contextId) map {
      case MongoSuccessCreate   => true
      case MongoFailedCreate    => false
    }
  }

  def getByKey(sessionId : String, key : String)(implicit format : OFormat[Session]) : Future[String] = {
    for {
      session <- sessionRepo.getSession(sessionId)
      _       <- sessionRepo.renewSession(sessionId)
    } yield session.fold(throw new MissingSessionException(s"No session for sessionId $sessionId")) { session =>
      session.data.getOrElse(key, throw new SessionKeyNotFoundException(s"No data found for key $key for sessionId $sessionId"))
    }
  }

  def updateDataKey(sessionId : String, updateSet: UpdateSet): Future[MongoUpdatedResponse] = {
    sessionRepo.updateSession(sessionId, updateSet)
  }

  def destroySessionRecord(sessionId : String): Future[Boolean] = {
    sessionRepo.removeSession(sessionId) map {
      case MongoSuccessDelete   => true
      case MongoFailedDelete    => false
    }
  }
}
