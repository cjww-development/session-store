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

package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.cjwwdev.bootstrap.config.BaseConfiguration
import com.cjwwdev.reactivemongo.{MongoFailedUpdate, MongoSuccessUpdate}
import com.cjwwdev.security.encryption.DataSecurity
import models.{Session, SessionTimestamps, UpdateSet}
import org.joda.time.DateTime
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers
import play.api.libs.json.OFormat
import play.api.libs.ws.ahc.AhcWSClient
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService

import scala.concurrent.Future

class SessionControllerSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with BaseConfiguration {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val ws = AhcWSClient()

  val mockSessionService = mock[SessionService]

  val testSession = Session(
    sessionId         = "test-session-id",
    data              = Map("testKey" -> "testData"),
    modifiedDetails   = SessionTimestamps(
      created         = DateTime.now,
      lastModified    = DateTime.now
    )
  )

  class Setup {
    val testController = new SessionController(mockSessionService) {
      override protected def validateSession(id: String)(f: (Session) => Future[Result])(implicit format: OFormat[Session]): Future[Result] = {
        f(testSession)
      }
    }
  }

  "cache" should {
    "return a created" when {
      "the session has been cached" in new Setup {
        when(mockSessionService.cacheData(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(true))

        val result = testController.cache("test-session-id")(FakeRequest().withHeaders(CONTENT_TYPE -> TEXT, "appId" -> AUTH_SERVICE_ID))
        status(result.run()) mustBe CREATED
      }
    }

    "return an internal server error" when {
      "there was a problem caching the data" in new Setup {
        when(mockSessionService.cacheData(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(false))

        val result = testController.cache("test-session-id")(FakeRequest().withHeaders(CONTENT_TYPE -> TEXT, "appId" -> AUTH_SERVICE_ID))
        status(result.run()) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return a forbidden" when {
      "there is no appId in the headers" in new Setup {
        val result = testController.cache("test-session-id")(FakeRequest().withHeaders(CONTENT_TYPE -> TEXT))
        status(result.run()) mustBe FORBIDDEN
      }
    }
  }

  "getEntry" should {
    "return an OK" when {
      "data was found matching the specified key" in new Setup {
        when(mockSessionService.getByKey(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some("testData")))

        val result = testController.getEntry("test-session-id", "test-key")(FakeRequest().withHeaders(CONTENT_TYPE -> TEXT, "appId" -> AUTH_SERVICE_ID))
        status(result) mustBe OK
      }
    }

    "return a Not found" when {
      "no data was found matching the specified key" in new Setup {
        when(mockSessionService.getByKey(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))

        val result = testController.getEntry("test-session-id", "test-key")(FakeRequest().withHeaders(CONTENT_TYPE -> TEXT, "appId" -> AUTH_SERVICE_ID))
        status(result) mustBe NOT_FOUND
      }
    }

    "return a forbidden" when {
      "there is no appId in the headers" in new Setup {
        val result = testController.getEntry("test-session-id", "test-key")(FakeRequest().withHeaders(CONTENT_TYPE -> TEXT))
        status(result) mustBe FORBIDDEN
      }
    }
  }

  "updateSession" should {
    "return an ok" when {
      "the session was successfully updated" in new Setup {
        when(mockSessionService.updateDataKey(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(MongoSuccessUpdate))

        val result = testController.updateSession("test-session-id")(FakeRequest()
          .withHeaders(CONTENT_TYPE -> TEXT, "appId" -> AUTH_SERVICE_ID)
          .withBody(DataSecurity.encryptData(UpdateSet("testKey","testData")).get)
        )
        status(result) mustBe OK
      }
    }

    "return an internal server error" when {
      "there a problem updating the session" in new Setup {
        when(mockSessionService.updateDataKey(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(MongoFailedUpdate))

        val result = testController.updateSession("test-session-id")(FakeRequest()
          .withHeaders(CONTENT_TYPE -> TEXT, "appId" -> AUTH_SERVICE_ID)
          .withBody(DataSecurity.encryptData(UpdateSet("testKey","testData")).get)
        )
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return a forbidden" when {
      "there is no appId in the headers" in new Setup {
        val result = testController.updateSession("test-session-id")(FakeRequest().withHeaders(CONTENT_TYPE -> TEXT))
        status(result.run()) mustBe FORBIDDEN
      }
    }
  }

  "destroy" should {
    "return an ok" when {
      "the session was successfully destroyed" in new Setup {
        when(mockSessionService.destroySessionRecord(ArgumentMatchers.any()))
          .thenReturn(Future.successful(true))

        val result = testController.destroy("test-session-id")(FakeRequest().withHeaders(CONTENT_TYPE -> TEXT, "appId" -> AUTH_SERVICE_ID))
        status(result) mustBe OK
      }
    }

    "return an internal server error" when {
      "there was a problem destroying the session" in new Setup {
        when(mockSessionService.destroySessionRecord(ArgumentMatchers.any()))
          .thenReturn(Future.successful(false))

        val result = testController.destroy("test-session-id")(FakeRequest().withHeaders(CONTENT_TYPE -> TEXT, "appId" -> AUTH_SERVICE_ID))
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return a forbidden" when {
      "there is no appId in the header" in new Setup {
        val result = testController.destroy("test-session-id")(FakeRequest().withHeaders(CONTENT_TYPE -> TEXT))
        status(result) mustBe FORBIDDEN
      }
    }
  }
}
