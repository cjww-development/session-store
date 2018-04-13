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

package services

import com.cjwwdev.logging.Logging
import com.cjwwdev.mongo.responses._
import common.MissingSessionException
import javax.inject.Inject
import models.Session
import play.api.libs.json.OFormat
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionServiceImpl @Inject()(val sessionRepo: SessionRepository) extends SessionService

trait SessionService extends Logging {
  val sessionRepo: SessionRepository

  def cacheData(sessionId: String): Future[Boolean] = {
    sessionRepo.cacheData(sessionId) map {
      case MongoSuccessCreate   => true
      case MongoFailedCreate    => false
    }
  }

  def getByKey(sessionId : String, key : String)(implicit format : OFormat[Session]) : Future[Option[String]] = {
    for {
      session <- sessionRepo.getSession(sessionId)
      _       <- sessionRepo.renewSession(sessionId)
    } yield session.fold(throw new MissingSessionException(s"No session for sessionId $sessionId"))(_.data.get(key))
  }

  def getSession(sessionId: String): Future[Option[Session]] = {
    sessionRepo.getSession(sessionId)
  }

  def updateDataKey(sessionId : String, updateSet: Map[String, String]): Future[Seq[(String, String)]] = {
    Future.sequence(
      updateSet.toList.map(update => sessionRepo.updateSession(sessionId, update._1, update._2))
    )
  }

  def destroySessionRecord(sessionId : String): Future[Boolean] = {
    sessionRepo.removeSession(sessionId) map {
      case MongoSuccessDelete   => true
      case MongoFailedDelete    => false
    }
  }

  def cleanseSessions: Future[MongoDeleteResponse] = {
    for {
      toRemove <- sessionRepo.getSessions map { list =>
        val sessionsToRemove = list.filter(_.hasTimedOut)
        logger.info(s"Current sessions: ${list.size}")
        logger.info(s"Sessions to remove ${sessionsToRemove.size}")
        sessionsToRemove
      }
      _ = toRemove.foreach(x => sessionRepo.removeSession(x.sessionId))
    } yield MongoSuccessDelete
  }
}
