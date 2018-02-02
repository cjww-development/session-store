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

package services

import java.util.UUID

import com.cjwwdev.implicits.ImplicitHandlers
import com.cjwwdev.reactivemongo._
import com.cjwwdev.test.CJWWSpec
import models.{Session, SessionTimestamps, UpdateSet}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionServiceSpec extends CJWWSpec {

  val mockRepo = mock[SessionRepository]

  val testUpdateSet = UpdateSet("userInfo","testData")

  val dateFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  val dateTime: DateTime = dateFormat.parseDateTime("2017-04-13 19:59:14")

  val testSession = Session(
    sessionId       = "testSessionId",
    data            = Map("testKey" -> "testData"),
    modifiedDetails = SessionTimestamps(
      created      = dateTime,
      lastModified = dateTime
    )
  )

  val uuid = UUID.randomUUID()

  class Setup extends ImplicitHandlers {
    val testService = new SessionService {
      override val sessionRepo = mockRepo
    }
  }

  "cacheData" should {
    "return true if data is successfully saved" in new Setup {
      when(mockRepo.cacheData(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future(MongoSuccessCreate))

      val result = testService.cacheData("sessionID", "testContextId".encrypt)
      await(result) mustBe true
    }

    "return false if there was a problem saving" in new Setup {
      when(mockRepo.cacheData(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future(MongoFailedCreate))

      val result = testService.cacheData("sessionID", "testContextId".encrypt)
      await(result) mustBe false
    }
  }

  "getByKey" should {
    "return an optional string" in new Setup {
      when(mockRepo.getSession(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future(Some(testSession)))

      when(mockRepo.renewSession(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future(MongoSuccessUpdate))

      val result = testService.getByKey("testSessionId", "testKey")
      await(result) mustBe "testData"
    }
  }

  "updateDataKey" should {
    "return an UpdateWriteResult" when {
      "given a sessionID, a key and data" in new Setup {
        when(mockRepo.updateSession(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future(MongoSuccessUpdate))

        val result = testService.updateDataKey("testID", testUpdateSet)
        await(result) mustBe MongoSuccessUpdate
      }
    }
  }

  "destroySessionRecord" should {
    "remove a session record" when {
      "given a valid session id" in new Setup {
        when(mockRepo.removeSession(ArgumentMatchers.any()))
          .thenReturn(Future(MongoSuccessDelete))

        val result = testService.destroySessionRecord("sessionID")
        await(result) mustBe true
      }
    }

    "return a MongoFailedDelete" when {
      "there was a problem deleting the session record" in new Setup {
        when(mockRepo.removeSession(ArgumentMatchers.any()))
          .thenReturn(Future(MongoFailedDelete))

        val result = testService.destroySessionRecord("sessionID")
        await(result) mustBe false
      }
    }
  }
}
