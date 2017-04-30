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

// $COVERAGE-OFF$
package repositories

import javax.inject.{Inject, Singleton}

import com.cjwwdev.logging.Logger
import com.cjwwdev.reactivemongo._
import config.Exceptions.{MissingSessionException, SessionKeyNotFoundException}
import models.{Session, UpdateSet}
import play.api.libs.json.OFormat
import reactivemongo.api.DB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json._
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SessionRepository @Inject()() extends MongoConnector {
  val store = new SessionRepo(db)
}

class SessionRepo(db: () => DB) extends MongoRepository("session-cache", db) {

  override def indexes: Seq[Index] = Seq(
    Index(
      key = Seq("sessionId" -> IndexType.Ascending),
      name = Some("SessionId"),
      unique = true,
      sparse = false
    )
  )

  private def sessionIdSelector(sessionId: String) = BSONDocument("sessionId" -> sessionId)

  def cacheData(sessionID : String, data : String) : Future[MongoCreateResponse] = {
    val now = Session.getDateTime
    val dataEntry = Session(sessionID, Map("userInfo" -> data), Map("created" -> now, "lastModified" -> now))

    collection.insert(dataEntry) map { writeResult =>
      if(writeResult.ok) {
        Logger.info(s"[SessionRepository] - [cacheData] : Data was successfully created in ${collection.name}")
        MongoSuccessCreate
      } else {
        Logger.error(s"[SessionRepository] - [cacheData] : There was a problem inserting data into ${collection.name}]")
        MongoFailedCreate
      }
    }
  }

  def getData(sessionID : String, key : String)(implicit format: OFormat[Session]) : Future[String] = {
    collection.find(sessionIdSelector(sessionID)).one[Session] map {
      case Some(session) => session.data.get(key) match {
        case Some(kv) => kv
        case None => throw new SessionKeyNotFoundException(s"Data for key $key could not be found in session for $sessionID")
      }
      case None => throw new MissingSessionException(s"No session found for session id $sessionID")
    }
  }

  def getSession(sessionId: String)(implicit format: OFormat[Session]): Future[Option[Session]] = {
    collection.find(sessionIdSelector(sessionId)).one[Session]
  }

  def updateSession(sessionId: String, updateSet: UpdateSet)(implicit format: OFormat[Session]): Future[MongoUpdatedResponse] = {
    collection.find(sessionIdSelector(sessionId)).one[Session] flatMap {
      case Some(session) =>
        val updated = session.copy(
          data = session.data + (updateSet.key -> updateSet.data),
          modifiedDetails = session.modifiedDetails. +("lastModified" -> Session.getDateTime)
        )
        collection.update(sessionIdSelector(sessionId), updated) map { writeResult =>
          if(writeResult.ok) {
            Logger.info(s"[SessionRepository] - [updateSession] : Successfully updated session for session id $sessionId")
            MongoSuccessUpdate
          } else {
            Logger.error(s"[SessionRepository] - [updateSession] : There was a problem updating session for session id $sessionId")
            MongoFailedUpdate
          }
        }
      case None => throw new MissingSessionException(s"No session found for session id $sessionId")
    }
  }

  def removeSession(sessionId: String): Future[MongoDeleteResponse] = {
    collection.remove(sessionIdSelector(sessionId)) map { writeResult =>
      if(writeResult.ok) {
        Logger.info(s"[SessionRepository] - [removeSession] : Successfully removed session $sessionId")

        MongoSuccessDelete
      } else {
        Logger.error(s"[SessionRepository] - [removeSession] : There was a problem deleting session $sessionId")
        MongoFailedDelete
      }
    }
  }
}
