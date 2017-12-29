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

import javax.inject.Inject

import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.reactivemongo._
import models.{Session, SessionTimestamps, UpdateSet}
import play.api.libs.json.{JsValue, OFormat}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionRepositoryImpl @Inject()(val configurationLoader: ConfigurationLoader) extends SessionRepository

trait SessionRepository extends MongoDatabase {
  override def indexes: Seq[Index] = Seq(
    Index(
      key    = Seq("sessionId" -> IndexType.Ascending),
      name   = Some("SessionId"),
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
    sessionId       = sessionId,
    data            = Map("contextId" -> contextId),
    modifiedDetails = SessionTimestamps(Session.getDateTime, Session.getDateTime)
  )

  def cacheData(sessionId : String, data : String): Future[MongoCreateResponse] = {
    for {
      col <- collection
      res <- col.insert(buildNewSession(sessionId, data))
    } yield if(res.ok) {
      logger.info(s"[cacheData] : Data was successfully created in ${col.name} for sessionId $sessionId")
      MongoSuccessCreate
    } else {
      logger.error(s"[cacheData] : There was a problem inserting data into ${col.name} for sessionId $sessionId")
      MongoFailedCreate
    }
  }

  def getSession(sessionId: String)(implicit format: OFormat[Session]): Future[Option[Session]] = {
    for {
      col <- collection
      res <- col.find(sessionIdSelector(sessionId)).one[Session]
    } yield res
  }

  def updateSession(sessionId: String, updateSet: UpdateSet)(implicit format: OFormat[Session]): Future[MongoUpdatedResponse] = {
    for {
      col <- collection
      res <- col.update(sessionIdSelector(sessionId), buildUpdateDocument(updateSet))
    } yield if(res.ok) {
      logger.info(s"[updateSession] : Successfully updated session for session id $sessionId")
      MongoSuccessUpdate
    } else {
      logger.error(s"[updateSession] : There was a problem updating session for session id $sessionId")
      MongoFailedUpdate
    }
  }

  def removeSession(sessionId: String): Future[MongoDeleteResponse] = {
    for {
      col <- collection
      res <- col.remove(sessionIdSelector(sessionId))
    } yield if(res.ok) {
      logger.info(s"[removeSession] : Successfully removed session $sessionId")
      MongoSuccessDelete
    } else {
      logger.error(s"[removeSession] : There was a problem deleting session $sessionId")
      MongoFailedDelete
    }
  }

  def validateSession(sessionId: String): Future[Boolean] = {
    for {
      col     <- collection
      session <- col.find[BSONDocument, BSONDocument](BSONDocument("sessionId" -> sessionId), BSONDocument("sessionId" -> 1, "_id" -> 0)).one[JsValue]
    } yield session.nonEmpty
  }
}
