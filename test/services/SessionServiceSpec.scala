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

import com.cjwwdev.implicits.ImplicitHandlers
import com.cjwwdev.mongo.responses._
import common.MissingSessionException
import helpers.repositories.MockSessionRepository
import helpers.services.ServicesSpec
import models.{Session, SessionTimestamps}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

class SessionServiceSpec extends ServicesSpec with MockSessionRepository with ImplicitHandlers {
  val dateFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  override val dateTime: DateTime   = dateFormat.parseDateTime("2017-04-13 19:59:14")

  override val testSession = Session(
    sessionId       = "testSessionId",
    data            = Map("testKey" -> "testData"),
    modifiedDetails = SessionTimestamps(
      created      = dateTime,
      lastModified = dateTime
    )
  )

  val testService = new SessionService {
    override val sessionRepo = mockSessionRepository
  }

  "cacheData" should {
    "return true if data is successfully saved" in {
      mockCacheDataRepository(true)

      awaitAndAssert(testService.cacheData(testSessionId)) {
        _ mustBe true
      }
    }

    "return false if there was a problem saving" in {
      mockCacheDataRepository(false)

      awaitAndAssert(testService.cacheData(testSessionId)) {
        _ mustBe false
      }
    }
  }

  "getByKey" should {
    "return an optional string" in {
      mockGetSession(Some(testSession))

      mockRenewSession(true)

      awaitAndAssert(testService.getByKey(testSessionId, "testKey")) {
        _ mustBe Some("testData")
      }
    }

    "return None" in {
      mockGetSession(Some(testSession))

      mockRenewSession(true)

      awaitAndAssert(testService.getByKey(testSessionId, "testInvalidKey")) {
        _ mustBe None
      }
    }

    "throw a MissingSessionException if the given session can't be found" in {
      mockGetSession(None)

      mockRenewSession(false)

      intercept[MissingSessionException](await(testService.getByKey("testSessionId", "testKey")))
    }
  }

  "getSession" should {
    "return some session" in {
      mockGetSession(session = Some(testSession))

      awaitAndAssert(testService.getSession(testSessionId)) {
        _ mustBe Some(testSession)
      }
    }

    "return None" in {
      mockGetSession(session = None)

      awaitAndAssert(testService.getSession(testSessionId)) {
        _ mustBe None
      }
    }
  }

  "updateDataKey" should {
    "return an UpdateWriteResult" when {
      "given a sessionID, a key and data" in {
        mockUpdateSession(true)

        awaitAndAssert(testService.updateDataKey(testSessionId, Map("key" -> "testData"))) {
          _ mustBe List("key" -> MongoSuccessUpdate.toString)
        }
      }
    }
  }

  "destroySessionRecord" should {
    "remove a session record" when {
      "given a valid session id" in {
        mockRemoveSession(true)

        awaitAndAssert(testService.destroySessionRecord(testSessionId)) { result =>
          assert(result)
        }
      }
    }

    "return a MongoFailedDelete" when {
      "there was a problem deleting the session record" in {
        mockRemoveSession(false)

        awaitAndAssert(testService.destroySessionRecord(testSessionId)) { result =>
          assert(!result)
        }
      }
    }
  }

  "cleanseSessions" should {
    "return a MongoSuccessDelete" in {
      mockGetSessions(List(testSession))

      mockRemoveSession(true)

      awaitAndAssert(testService.cleanseSessions) {
        _ mustBe MongoSuccessDelete
      }
    }
  }
}
