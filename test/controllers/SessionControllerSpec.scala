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
 */

package controllers

import builders.AuthBuilder
import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.implicits.ImplicitHandlers
import com.cjwwdev.reactivemongo.{MongoFailedUpdate, MongoSuccessUpdate}
import com.cjwwdev.security.encryption.DataSecurity
import com.cjwwdev.test.CJWWSpec
import com.cjwwdev.test.data.TestDataGenerator
import config.SessionKeyNotFoundException
import models.{Session, SessionTimestamps, UpdateSet}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SessionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionControllerSpec extends CJWWSpec with TestDataGenerator with ImplicitHandlers {

  val mockSessionService = mock[SessionService]
  val mockSessionRepo    = mock[SessionRepository]

  val testSessionId = generateTestSystemId(SESSION)
  val testContextId = generateTestSystemId(CONTEXT)

  val dateFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  val dateTime: DateTime            = DateTime.now

  val testSession = Session(
    sessionId = testSessionId,
    data      = Map("contextId" -> testContextId),
    modifiedDetails = SessionTimestamps(
      created      = dateTime,
      lastModified = dateTime
    )
  )

  val testController = new SessionController {
    override val sessionService    = mockSessionService
    override val sessionRepository = mockSessionRepo
  }

  val cacheRequest = FakeRequest()
    .withHeaders(
      "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
      CONTENT_TYPE   -> TEXT
    ).withBody(testContextId.encrypt)

  val getEntryRequest = FakeRequest()
    .withHeaders(
      "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
      CONTENT_TYPE   -> TEXT
    )

  val updateSessionRequest = FakeRequest()
    .withHeaders(
      "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
      CONTENT_TYPE   -> TEXT
    ).withBody(
      DataSecurity.encryptType[UpdateSet](UpdateSet(
        key  = "testUpdateKey",
        data = "testUpdateData"
      ))
    )

  val destroySessionRequest = FakeRequest()
    .withHeaders(
      "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
      CONTENT_TYPE   -> TEXT
    )

  "cache" should {
    "return an ok" when {
      "the session has been validated and the request body data has been cached" in {
        when(mockSessionService.cacheData(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(true))

        val result = testController.cache(testSessionId)(cacheRequest)
        status(result) mustBe CREATED
      }
    }

    "return an internal server error" when {
      "the session has been validated but there was a problem caching the data" in {
        when(mockSessionService.cacheData(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(false))

        val result = testController.cache(testSessionId)(cacheRequest)
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "getEntry" should {
    "return an ok" when {
      "data has been found against the given key and session id" in {
        when(mockSessionService.getByKey(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future("testData"))

        AuthBuilder.actionWithUser(testController.getEntry(testSessionId, "testKey"), getEntryRequest, mockSessionRepo)(true, Some(testSession)) {
          result =>
            status(result) mustBe OK
            contentAsString(result) mustBe "testData"
        }
      }
    }

    "return a Not content" when {
      "no data has been found against the key and session id" in {
        when(mockSessionService.getByKey(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.failed(new SessionKeyNotFoundException("")))

        AuthBuilder.actionWithUser(testController.getEntry(testSessionId, "testKey"), getEntryRequest, mockSessionRepo)(true, Some(testSession)) {
          result => status(result) mustBe NO_CONTENT
        }
      }
    }
  }

  "updateSession" should {
    "return an ok" when {
      "the session has been updated" in {
        when(mockSessionService.updateDataKey(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(MongoSuccessUpdate))

        AuthBuilder.updateActionWithUser(testController.updateSession(testSessionId), updateSessionRequest, mockSessionRepo)(true, Some(testSession)) {
          result => status(result) mustBe CREATED
        }
      }
    }

    "return an internal server error" when {
      "there was a problem updating the session" in {
        when(mockSessionService.updateDataKey(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(MongoFailedUpdate))

        AuthBuilder.updateActionWithUser(testController.updateSession(testSessionId), updateSessionRequest, mockSessionRepo)(true, Some(testSession)) {
          result => status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "destroySession" should {
    "return an ok" when {
      "the given session has been destroyed" in {
        when(mockSessionService.destroySessionRecord(ArgumentMatchers.any()))
          .thenReturn(Future(true))

        AuthBuilder.actionWithUser(testController.destroy(testSessionId), destroySessionRequest, mockSessionRepo)(true, Some(testSession)) {
          result => status(result) mustBe OK
        }
      }
    }

    "return an internal server error" when {
      "there was a problem destroying the session" in {
        when(mockSessionService.destroySessionRecord(ArgumentMatchers.any()))
          .thenReturn(Future(false))

        AuthBuilder.actionWithUser(testController.destroy(testSessionId), destroySessionRequest, mockSessionRepo)(true, Some(testSession)) {
          result => status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "getContext" should {
    "return an ok" when {
      "the contextId for the given session has been found" in {
        AuthBuilder.actionWithUser(testController.getContextId(testSessionId), getEntryRequest, mockSessionRepo)(true, Some(testSession)) {
          result =>
            status(result) mustBe OK
            contentAsString(result).decrypt mustBe testContextId
        }
      }
    }
  }
}
