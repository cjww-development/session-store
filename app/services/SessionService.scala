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

import com.cjwwdev.reactivemongo._
import models.{Session, UpdateSet}
import play.api.libs.json.OFormat
import repositories.{SessionRepo, SessionRepository}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SessionService @Inject()(sessionRepo: SessionRepository) {

  val store: SessionRepo = sessionRepo.store

  def cacheData(sessionID: String, data: String): Future[Boolean] = {
    store.cacheData(sessionID, data) map {
      case MongoSuccessCreate   => true
      case MongoFailedCreate    => false
    }
  }

  def getByKey(sessionID : String, key : String)(implicit format : OFormat[Session]) : Future[Option[String]] = {
    store.getData(sessionID, key) map {
      data => Some(data)
    } recover {
      case _: Throwable => None
    }
  }

  def updateDataKey(sessionID : String, updateSet: UpdateSet): Future[MongoUpdatedResponse] = {
    store.updateSession(sessionID, updateSet)
  }

  def destroySessionRecord(sessionID : String) : Future[Boolean] = {
    store.removeSession(sessionID) map {
      case MongoSuccessDelete   => true
      case MongoFailedDelete    => false
    }
  }
}
