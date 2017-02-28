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
package repositories

import javax.inject.{Inject, Singleton}

import config.MongoCollections
import com.cjwwdev.mongo._
import models.InitialSession
import play.api.libs.json._
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SessionRepository @Inject()(injMongoConnector: MongoConnector) extends MongoCollections with MongoRepository {

  implicit val indexes = ensureIndex("sessionId", "sessionId", IndexType.Text)

  def cacheData(sessionID : String, data : String) : Future[MongoResponse] = {
    val now = InitialSession.getDateTime
    val dataEntry = InitialSession(sessionID, Map("userInfo" -> data), Map("created" -> now, "lastModified" -> now))
    injMongoConnector.create[InitialSession](SESSION_CACHE, dataEntry)
  }

  def getData(sessionID : String, key : String)(implicit format: OFormat[InitialSession]) : Future[Option[String]] = {
    val selector = BSONDocument("sessionId" -> sessionID)
    injMongoConnector.read[InitialSession](SESSION_CACHE, selector) map {
      case MongoSuccessRead(model) => model.asInstanceOf[InitialSession].data.get(key)
      case MongoFailedRead => None
      case _ => throw new IllegalStateException()
    }
  }

  def getSession(sessionID : String)(implicit format: OFormat[InitialSession]) : Future[Option[InitialSession]] = {
    val selector = BSONDocument("sessionId" -> sessionID)
    injMongoConnector.read[InitialSession](SESSION_CACHE, selector) map {
      case MongoSuccessRead(model) => Some(model.asInstanceOf[InitialSession])
      case MongoFailedRead => None
      case _ => throw new IllegalStateException()
    }
  }

  def updateSession(sessionID : String, session : InitialSession, key : String, updateData : String)
                   (implicit format: OFormat[InitialSession]) : Future[MongoResponse] = {
    val selector = BSONDocument("sessionId" -> sessionID)
    val updated =
      session.copy(
        data = session.data + (key -> updateData),
        modifiedDetails = session.modifiedDetails. +("lastModified" -> InitialSession.getDateTime)
      )
    injMongoConnector.update(SESSION_CACHE, selector, updated)
  }

  def removeSessionRecord(sessionId : String) : Future[MongoResponse] = {
    injMongoConnector.delete(SESSION_CACHE, BSONDocument("sessionId" -> sessionId))
  }
}
