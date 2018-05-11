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
import com.cjwwdev.implicits.ImplicitDataSecurity._
import helpers.controllers.ControllerSpec
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest
import reactivemongo.core.errors.DatabaseException

class SessionControllerSpec extends ControllerSpec {

  val testController = new SessionController {
    override val sessionService    = mockSessionService
    override val sessionRepository = mockSessionRepository
  }

  "initialiseSession" should {
    val cacheRequest = FakeRequest("POST", "/")
      .withHeaders(
        "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
        CONTENT_TYPE   -> TEXT
      ).withBody("")

    "return an ok" when {
      "the session has been validated and the request body data has been cached" in {
        mockCacheData(session = Some(testSession))

        assertFutureResult(testController.initialiseSession(testSessionId)(cacheRequest)) { result =>
          status(result) mustBe CREATED
          evaluateJsonResponse(POST, CREATED, Json.toJson(testSession))(notError = true)(contentAsJson(result))
        }
      }
    }

    "return an internal server error" when {
      "the session has been validated but there was a problem caching the data" in {
        mockCacheData(session = None)

        assertFutureResult(testController.initialiseSession(testSessionId)(cacheRequest)) { result =>
          status(result) mustBe INTERNAL_SERVER_ERROR
          evaluateJsonResponse(
            POST,
            INTERNAL_SERVER_ERROR,
            JsString(s"There was a problem caching the session data for session $testSessionId")
          )(notError = false)(contentAsJson(result))
        }
      }
    }

    "return a BadRequest" when {
      "there is already a session with a duplicate sessionId" in {
        mockCacheData(session = None, exception = Some(new DatabaseException {
          override def originalDocument = ???
          override def code: Option[Int] = Some(11000)
          override def message: String = "Duplicate key"
        }))

        assertFutureResult(testController.initialiseSession(testSessionId)(cacheRequest)) { result =>
          status(result) mustBe BAD_REQUEST
          evaluateJsonResponse(
            POST,
            BAD_REQUEST,
            JsString(s"A session already exists against sessionId $testSessionId")
          )(notError = false)(contentAsJson(result))
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

        runActionWithAuth(testController.getEntry(testSessionId, Some("testKey")), getEntryRequest, Some(testSession)) { result =>
          status(result) mustBe OK
          evaluateJsonResponse(
            GET,
            OK,
            JsString("testData")
          )(notError = true)(contentAsJson(result))
        }
      }

      "an entire session was found" in {
        mockGetServiceSession(session = Some(testSession))

        runActionWithAuth(testController.getEntry(testSessionId, None), getEntryRequest, Some(testSession)) { result =>
          status(result) mustBe OK
          evaluateJsonResponse(
            GET,
            OK,
            Json.toJson(testSession)
          )(notError = true)(contentAsJson(result))
        }
      }
    }

    "return a No content" when {
      "no data has been found against the key and session id" in {
        mockGetKey(sessionExists = true, keyExists = false)

        runActionWithAuth(testController.getEntry(testSessionId, Some("testKey")), getEntryRequest, Some(testSession)) { result =>
          status(result) mustBe NO_CONTENT
        }
      }
    }

    "return a NotFound" when {
      "when no session can be found" in {
        mockGetKey(sessionExists = false, keyExists = false)

        runActionWithAuth(testController.getEntry(testSessionId, Some("testKey")), getEntryRequest, Some(testSession)) { result =>
          status(result) mustBe NOT_FOUND
          evaluateJsonResponse(
            GET,
            NOT_FOUND,
            JsString(s"No session found for session $testSessionId")
          )(notError = false)(contentAsJson(result))
        }
      }

      "when no session can be found and no key is specified" in {
        mockGetServiceSession(session = None)

        runActionWithAuth(testController.getEntry(testSessionId, None), getEntryRequest, Some(testSession)) { result =>
          status(result) mustBe NOT_FOUND
          evaluateJsonResponse(
            GET,
            NOT_FOUND,
            JsString(s"No session found for session $testSessionId")
          )(notError = false)(contentAsJson(result))
        }
      }
    }
  }

  val updateSessionRequest = FakeRequest()
    .withHeaders(
      "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
      CONTENT_TYPE   -> TEXT
    ).withBody(
      """
        |{
        |   "testUpdateKey" : "testUpdateData"
        |}
      """.stripMargin
    )

  "updateSession" should {
    "return an ok" when {
      "the session has been updated" in {
        mockUpdateDataKey(false)

        runActionWithAuthStringBody(testController.updateSession(testSessionId), updateSessionRequest, Some(testSession)) { result =>
          status(result) mustBe OK
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
          status(result) mustBe NO_CONTENT
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
}
