/*
 * Copyright 2020 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import com.cjwwdev.logging.Logging
import com.cjwwdev.logging.output.Logger
import com.cjwwdev.mongo.responses._
import common.{NoMatchingSession, Response}
import javax.inject.Inject
import models.Session
import play.api.mvc.Request
import repositories.SessionRepository

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultSessionService @Inject()(val sessionRepo: SessionRepository) extends SessionService

trait SessionService extends Logger with Logging {
  val sessionRepo: SessionRepository

  def cacheData(sessionId: String)(implicit req: Request[_], ec: ExC): Future[Option[Session]] = {
    for {
      _       <- sessionRepo.cacheData(sessionId)
      session <- sessionRepo.getSession(sessionId)
    } yield session
  }

  def getByKey(sessionId : String, key : String)(implicit req: Request[_], ec: ExC): Future[Either[Option[String], Response]] = {
    for {
      session <- sessionRepo.getSession(sessionId)
      _       <- sessionRepo.renewSession(sessionId)
    } yield session.fold[Either[Option[String], Response]](Right(NoMatchingSession))(
      session => Left(session.data.get(key))
    )
  }

  def getSession(sessionId: String)(implicit req: Request[_], ec: ExC): Future[Option[Session]] = {
    sessionRepo.getSession(sessionId)
  }

  def updateDataKey(sessionId : String, updateSet: Map[String, String])(implicit req: Request[_], ec: ExC): Future[Seq[(String, String)]] = {
    Future.sequence(updateSet.toList.map {
      case (key, value) => sessionRepo.updateSession(sessionId, key, value)
    })
  }

  def destroySessionRecord(sessionId : String)(implicit req: Request[_], ec: ExC): Future[Boolean] = {
    sessionRepo.removeSession(sessionId) map {
      case MongoSuccessDelete => true
      case MongoFailedDelete  => false
    }
  }

  def cleanseSessions(implicit ec: ExC): Future[MongoDeleteResponse] = {
    for {
      toRemove <- sessionRepo.getSessions map { list =>
        val sessionsToRemove = list.filter(_.hasTimedOut)
        logger.info(s"Current sessions: ${list.size}")
        logger.info(s"Sessions to remove ${sessionsToRemove.size}")
        sessionsToRemove
      }
      _ <- Future.traverse(toRemove)(session => sessionRepo.cleanSession(session.sessionId))
    } yield MongoSuccessDelete
  }
}
