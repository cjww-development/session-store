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

import config.MongoCollections
import connectors.MongoConnector
import models.InitialSession
import play.api.Logger
import play.api.libs.json._
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SessionRepository extends SessionRepository {
  val mongoConnector = MongoConnector
}

trait SessionRepository extends MongoCollections {

  val mongoConnector : MongoConnector

  def cacheData(sessionID : String, data : String) : Future[WriteResult] = {
    val now = InitialSession.getDateTime
    val dataEntry = InitialSession(sessionID, Map("userInfo" -> data), Map("created" -> now, "lastModified" -> now))
    mongoConnector.create[InitialSession](SESSION_CACHE, dataEntry)
  }

  def getData(sessionID : String, key : String)(implicit format: OFormat[InitialSession]) : Future[Option[String]] = {
    val selector = BSONDocument("_id" -> sessionID)
    mongoConnector.read[InitialSession](SESSION_CACHE, selector) map {
      case Some(model) => model.data.get(key)
      case None => None
    }
  }

  def getSession(sessionID : String)(implicit format: OFormat[InitialSession]) : Future[Option[InitialSession]] = {
    val selector = BSONDocument("_id" -> sessionID)
    mongoConnector.read[InitialSession](SESSION_CACHE, selector)
  }

  def updateSession(sessionID : String, session : InitialSession, key : String, updateData : String)
                   (implicit format: OFormat[InitialSession]) : Future[UpdateWriteResult] = {

    val selector = BSONDocument("_id" -> sessionID)
    val updated = session.copy(data = Map(key -> updateData), modifiedDetails = session.modifiedDetails. +("lastModified" -> InitialSession.getDateTime))
    mongoConnector.update[InitialSession](SESSION_CACHE, selector, updated)
  }

  def removeSessionRecord(sessionId : String) : Future[WriteResult] = {
    mongoConnector.delete(SESSION_CACHE, BSONDocument("_id" -> sessionId))
  }
}
