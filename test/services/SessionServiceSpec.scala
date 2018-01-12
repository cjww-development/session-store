/*
 * Copyright 2018 CJWW Development
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
 *
 */

package services

import java.util.UUID

import com.cjwwdev.reactivemongo._
import com.cjwwdev.security.encryption.DataSecurity
import com.cjwwdev.test.CJWWSpec
import models.{Session, SessionTimestamps, UpdateSet}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class SessionServiceSpec extends CJWWSpec {

  val mockRepo = mock[SessionRepository]

  val testUpdateSet = UpdateSet("userInfo","testData")

  val dateFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  val dateTime: DateTime = dateFormat.parseDateTime("2017-04-13 19:59:14")

  val testSession = Session(
    sessionId = "testSessionId",
    data = Map("testKey" -> "testData"),
    modifiedDetails = SessionTimestamps(
      created = dateTime,
      lastModified = dateTime
    )
  )

  val uuid = UUID.randomUUID()

  class Setup {
    val testService = new SessionService {
      override val sessionRepo = mockRepo
    }
  }

  "cacheData" should {
    "return true if data is successfully saved" in new Setup {
      when(mockRepo.cacheData(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(MongoSuccessCreate))

      val result = testService.cacheData("sessionID", DataSecurity.encryptType[JsValue](Json.parse("""{"contextId" : "testContextId"}""")))
      Await.result(result, 5.seconds) mustBe true
    }

    "return false if there was a problem saving" in new Setup {
      when(mockRepo.cacheData(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(MongoFailedCreate))

      val result = testService.cacheData("sessionID", DataSecurity.encryptType[JsValue](Json.parse("""{"contextId" : "testContextId"}""")))
      Await.result(result, 5.seconds) mustBe false
    }
  }

  "getByKey" should {
    "return an optional string" in new Setup {
      when(mockRepo.getSession(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(testSession)))

      val result = Await.result(testService.getByKey("testSessionId", "testKey"), 5.seconds)
      result mustBe DataSecurity.encryptType[JsValue](Json.parse(s"""{"data" : "testData"}"""))
    }
  }

  "updateDataKey" should {
    "return an UpdateWriteResult" when {
      "given a sessionID, a key and data" in new Setup {
        when(mockRepo.updateSession(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(MongoSuccessUpdate))

        val result = Await.result(testService.updateDataKey("testID", testUpdateSet), 5.seconds)
        result mustBe MongoSuccessUpdate
      }
    }
  }

  "destroySessionRecord" should {
    "remove a session record" when {
      "given a valid session id" in new Setup {
        when(mockRepo.removeSession(ArgumentMatchers.any()))
          .thenReturn(Future.successful(MongoSuccessDelete))

        val result = testService.destroySessionRecord("sessionID")
        Await.result(result, 5.seconds) mustBe true
      }
    }

    "return a MongoFailedDelete" when {
      "there was a problem deleting the session record" in new Setup {
        when(mockRepo.removeSession(ArgumentMatchers.any()))
          .thenReturn(Future.successful(MongoFailedDelete))

        val result = testService.destroySessionRecord("sessionID")
        Await.result(result, 5.seconds) mustBe false
      }
    }
  }
}
