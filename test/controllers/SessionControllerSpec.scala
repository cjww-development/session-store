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

package controllers

import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.implicits.ImplicitHandlers
import com.cjwwdev.security.encryption.DataSecurity
import helpers.auth.AuthBuilder
import helpers.controllers.ControllerSpec
import helpers.other.{Fixtures, TestDataGenerator}
import helpers.repositories.MockSessionRepository
import helpers.services.MockSessionService
import models.UpdateSet
import play.api.test.FakeRequest

class SessionControllerSpec
  extends ControllerSpec
    with MockSessionService
    with MockSessionRepository
    with ImplicitHandlers
    with AuthBuilder {

  val testController = new SessionController {
    override val sessionService    = mockSessionService
    override val sessionRepository = mockSessionRepository
  }

  "cache" should {
    val cacheRequest = FakeRequest()
      .withHeaders(
        "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
        CONTENT_TYPE   -> TEXT
      ).withBody(
        testContextId.encrypt
      )

    "return an ok" when {
      "the session has been validated and the request body data has been cached" in {
        mockCacheData(true)

        assertFutureResult(testController.cache(testSessionId)(cacheRequest)) { result =>
          status(result) mustBe CREATED
        }
      }
    }

    "return an internal server error" when {
      "the session has been validated but there was a problem caching the data" in {
        mockCacheData(false)

        assertFutureResult(testController.cache(testSessionId)(cacheRequest)) { result =>
          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "getEntry" should {
    val getEntryRequest = FakeRequest()
      .withHeaders(
        "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
        CONTENT_TYPE   -> TEXT
      )

    "return an ok" when {
      "data has been found against the given key and session id" in {
        mockGetKey(sessionExists = true, keyExists = true)

        runActionWithAuth(testController.getEntry(testSessionId, "testKey"), getEntryRequest, Some(testSession)) { result =>
          status(result) mustBe OK
          contentAsString(result) mustBe "testData"
        }
      }
    }

    "return a No content" when {
      "no data has been found against the key and session id" in {
        mockGetKey(sessionExists = true, keyExists = false)

        runActionWithAuth(testController.getEntry(testSessionId, "testKey"), getEntryRequest, Some(testSession)) { result =>
          status(result) mustBe NO_CONTENT
        }
      }
    }
  }

  val updateSessionRequest = FakeRequest()
    .withHeaders(
      "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
      CONTENT_TYPE   -> TEXT,
      "testHeader"   -> "qwerty"
    ).withBody(
      UpdateSet(key  = "testUpdateKey", data = "testUpdateData").encryptType
    )

  "updateSession" should {
    "return an ok" when {
      "the session has been updated" in {
        mockUpdateDataKey(false)

        runActionWithAuthStringBody(testController.updateSession(testSessionId), updateSessionRequest, Some(testSession)) { result =>
          status(result) mustBe CREATED
        }
      }
    }

    "return an internal server error" when {
      "there was a problem updating the session" in {
        mockUpdateDataKey(true)

        runActionWithAuthStringBody(testController.updateSession(testSessionId), updateSessionRequest, Some(testSession)) { result =>
          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "destroySession" should {
    val destroySessionRequest = FakeRequest()
      .withHeaders(
        "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
        CONTENT_TYPE   -> TEXT
      )

    "return an ok" when {
      "the given session has been destroyed" in {
        mockDestroySessionRecord(true)

        runActionWithAuth(testController.destroy(testSessionId), destroySessionRequest, Some(testSession)) { result =>
          status(result) mustBe OK
        }
      }
    }

    "return an internal server error" when {
      "there was a problem destroying the session" in {
        mockDestroySessionRecord(false)

        runActionWithAuth(testController.destroy(testSessionId), destroySessionRequest, Some(testSession)) { result =>
          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "getContext" should {
    "return an ok" when {
      "the contextId for the given session has been found" in {
        val getContextRequest = FakeRequest()
          .withHeaders(
            "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
            CONTENT_TYPE   -> TEXT
          )

        runActionWithAuth(testController.getContextId(testSessionId), getContextRequest, Some(testSession)) { result =>
          status(result) mustBe OK
          contentAsString(result).decrypt mustBe testContextId
        }
      }
    }
  }
}
