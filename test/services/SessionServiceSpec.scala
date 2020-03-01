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

import com.cjwwdev.mongo.responses._
import common.NoMatchingSession
import helpers.services.ServicesSpec
import models.{Session, SessionTimestamps}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext.Implicits.global

class SessionServiceSpec extends ServicesSpec {
  val dateFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  override val dateTime: DateTime   = dateFormat.parseDateTime("2017-04-13 19:59:14")

  implicit val request = FakeRequest()

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
    "return a session if data is successfully saved" in {
      mockCacheDataRepository(success = true)
      mockGetSession(session = Some(testSession))

      awaitAndAssert(testService.cacheData(testSessionId)) {
        _ mustBe Some(testSession)
      }
    }

    "return no session if there was a problem saving" in {
      mockCacheDataRepository(success = false)
      mockGetSession(session = None)

      awaitAndAssert(testService.cacheData(testSessionId)) {
        _ mustBe None
      }
    }
  }

  "getByKey" should {
    "return an optional string" in {
      mockGetSession(session = Some(testSession))

      mockRenewSession(success = true)

      awaitAndAssert(testService.getByKey(testSessionId, "testKey")) {
        _ mustBe Left(Some("testData"))
      }
    }

    "return None" in {
      mockGetSession(session = Some(testSession))

      mockRenewSession(success = true)

      awaitAndAssert(testService.getByKey(testSessionId, "testInvalidKey")) {
        _ mustBe Left(None)
      }
    }

    "return a NoMatchingSession if the given session can't be found" in {
      mockGetSession(session = None)

      mockRenewSession(success = false)

      awaitAndAssert(testService.getByKey("testSessionId", "testKey")) {
        _ mustBe Right(NoMatchingSession)
      }
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
        mockUpdateSession(success = true)

        awaitAndAssert(testService.updateDataKey(testSessionId, Map("key" -> "testData"))) {
          _ mustBe List("key" -> MongoSuccessUpdate.toString)
        }
      }
    }
  }

  "destroySessionRecord" should {
    "remove a session record" when {
      "given a valid session id" in {
        mockRemoveSession(success = true)

        awaitAndAssert(testService.destroySessionRecord(testSessionId)) { result =>
          assert(result)
        }
      }
    }

    "return a MongoFailedDelete" when {
      "there was a problem deleting the session record" in {
        mockRemoveSession(success = false)

        awaitAndAssert(testService.destroySessionRecord(testSessionId)) { result =>
          assert(!result)
        }
      }
    }
  }

  "cleanseSessions" should {
    "return a MongoSuccessDelete" in {
      mockGetSessions(sessions = List(testSession))

      mockCleanSession(success = true)

      awaitAndAssert(testService.cleanseSessions) {
        _ mustBe MongoSuccessDelete
      }
    }
  }
}
