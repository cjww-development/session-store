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
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

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
}
