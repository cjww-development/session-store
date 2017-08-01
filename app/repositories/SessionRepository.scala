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

package repositories

import javax.inject.{Inject, Singleton}

import com.cjwwdev.reactivemongo._
import config.{MissingSessionException, SessionKeyNotFoundException}
import models.{Session, SessionTimestamps, UpdateSet}
import play.api.libs.json.OFormat
import play.api.Logger
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json._
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SessionRepository @Inject()() extends MongoDatabase("session-cache") {

  override def indexes: Seq[Index] = Seq(
    Index(
      key = Seq("sessionId" -> IndexType.Ascending),
      name = Some("SessionId"),
      unique = true,
      sparse = false
    )
  )

  private def sessionIdSelector(sessionId: String) = BSONDocument("sessionId" -> sessionId)

  private def buildUpdateDocument(updateSet: UpdateSet) = BSONDocument(
    "$set" -> BSONDocument(
      s"data.${updateSet.key}" -> updateSet.data,
      "modifiedDetails.lastModified" -> BSONDocument(
        "$date" -> Session.getDateTime.getMillis
      )
    )
  )

  private def buildNewSession(sessionId: String, contextId: String) = Session(
    sessionId = sessionId,
    data = Map("contextId" -> contextId),
    modifiedDetails = SessionTimestamps(Session.getDateTime, Session.getDateTime)
  )

  def cacheData(sessionId : String, data : String) : Future[MongoCreateResponse] = {
    collection flatMap { collect =>
      collect.insert(buildNewSession(sessionId, data)) map { wr =>
        if(wr.ok) {
          Logger.info(s"[SessionRepository] - [cacheData] : Data was successfully created in ${collect.name}")
          MongoSuccessCreate
        } else {
          Logger.error(s"[SessionRepository] - [cacheData] : There was a problem inserting data into ${collect.name}]")
          MongoFailedCreate
        }
      }
    }
  }

  def getData(sessionId : String, key : String)(implicit format: OFormat[Session]) : Future[String] = {
    collection flatMap {
      _.find(sessionIdSelector(sessionId)).one[Session] map {
        case Some(session) => session.data.get(key) match {
          case Some(pair) => pair
          case None => throw new SessionKeyNotFoundException(s"Data for key $key could not be found in session for $sessionId")
        }
        case None => throw new MissingSessionException(s"No session found for session id $sessionId")
      }
    }
  }

  def getSession(sessionId: String)(implicit format: OFormat[Session]): Future[Option[Session]] = {
    collection.flatMap(_.find(sessionIdSelector(sessionId)).one[Session])
  }

  def updateSession(sessionId: String, updateSet: UpdateSet)(implicit format: OFormat[Session]): Future[MongoUpdatedResponse] = {
    collection flatMap {
      _.update(sessionIdSelector(sessionId), buildUpdateDocument(updateSet)) map { wr =>
        if(wr.ok) {
          Logger.info(s"[SessionRepository] - [updateSession] : Successfully updated session for session id $sessionId")
          MongoSuccessUpdate
        } else {
          Logger.error(s"[SessionRepository] - [updateSession] : There was a problem updating session for session id $sessionId")
          MongoFailedUpdate
        }
      }
    }
  }

  def removeSession(sessionId: String): Future[MongoDeleteResponse] = {
    collection flatMap {
      _.remove(sessionIdSelector(sessionId)) map { writeResult =>
        if (writeResult.ok) {
          Logger.info(s"[SessionRepository] - [removeSession] : Successfully removed session $sessionId")
          MongoSuccessDelete
        } else {
          Logger.error(s"[SessionRepository] - [removeSession] : There was a problem deleting session $sessionId")
          MongoFailedDelete
        }
      }
    }
  }
}
