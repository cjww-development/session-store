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

package repositories

import com.cjwwdev.logging.Logging
import com.cjwwdev.mongo.DatabaseRepository
import com.cjwwdev.mongo.connection.ConnectionSettings
import com.cjwwdev.mongo.responses._
import javax.inject.Inject
import models.{Session, SessionTimestamps}
import play.api.Configuration
import play.api.libs.json.{JsValue, OFormat}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DefaultSessionRepository @Inject()(val config: Configuration) extends SessionRepository with ConnectionSettings

trait SessionRepository extends DatabaseRepository with Logging {

  override def indexes: Seq[Index] = Seq(
    Index(
      key    = Seq("sessionId" -> IndexType.Ascending),
      name   = Some("SessionId"),
      unique = true,
      sparse = false
    )
  )

  private def sessionIdSelector(sessionId: String) = BSONDocument("sessionId" -> sessionId)

  private def buildUpdateDocument(key: String, data: String) = BSONDocument(
    "$set" -> BSONDocument(
      s"data.$key" -> data,
      "modifiedDetails.lastModified" -> BSONDocument(
        "$date" -> Session.getDateTime.getMillis
      )
    )
  )

  private def buildNewSession(sessionId: String) = Session(
    sessionId       = sessionId,
    data            = Map.empty[String, String],
    modifiedDetails = SessionTimestamps(Session.getDateTime, Session.getDateTime)
  )

  def cacheData(sessionId : String): Future[MongoCreateResponse] = {
    for {
      col <- collection
      res <- col.insert(buildNewSession(sessionId))
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

  def renewSession(sessionId: String)(implicit format: OFormat[Session]): Future[MongoUpdatedResponse] = {
    for {
      col <- collection
      res <- col.update(sessionIdSelector(sessionId), BSONDocument(
        "$set" -> BSONDocument("modifiedDetails.lastModified" -> BSONDocument("$date" -> Session.getDateTime.getMillis)))
      )
    } yield if (res.ok) {
      logger.info(s"[renewSession] : Successfully renewed session for session id $sessionId")
      MongoSuccessUpdate
    } else {
      logger.error(s"[renewSession] : There was a problem renewing session for session id $sessionId")
      MongoFailedUpdate
    }
  }

  def getSessions(implicit format: OFormat[Session]): Future[List[Session]] = {
    for {
      col <- collection
      res <- col.find(BSONDocument()).cursor[Session]().collect[List]()
    } yield res
  }

  def updateSession(sessionId: String, key: String, data: String)(implicit format: OFormat[Session]): Future[(String, String)] = {
    for {
      col <- collection
      res <- col.update(sessionIdSelector(sessionId), buildUpdateDocument(key, data))
    } yield if(res.ok) {
      logger.info(s"[updateSession] : Successfully updated session for session id $sessionId")
      key -> MongoSuccessUpdate.toString
    } else {
      logger.error(s"[updateSession] : There was a problem updating session for session id $sessionId")
      key -> MongoFailedUpdate.toString
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
