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

import com.cjwwdev.reactivemongo._
import config.Exceptions.SessionKeyNotFoundException
import models.UpdateSet
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import repositories.{SessionRepo, SessionRepository}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class SessionServiceSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {

  val mockRepo = mock[SessionRepository]
  val mockStore = mock[SessionRepo]

  val testUpdateSet = UpdateSet("userInfo","testData")

  class Setup {
    val testService = new SessionService(mockRepo) {
      override val store: SessionRepo = mockStore
    }
  }

  "cacheData" should {
    "return true if data is successfully saved" in new Setup {
      when(mockStore.cacheData(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(MongoSuccessCreate))

      val result = testService.cacheData("sessionID", "testData")
      Await.result(result, 5.seconds) mustBe true
    }

    "return false if there was a problem saving" in new Setup {
      when(mockStore.cacheData(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(MongoFailedCreate))

      val result = testService.cacheData("sessionID", "testData")
      Await.result(result, 5.seconds) mustBe false
    }
  }

  "getByKey" should {
    "return an optional string" in new Setup {
      when(mockStore.getData(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful("testData"))

      val result = Await.result(testService.getByKey("sessionID", "testKey"), 5.seconds)
      result mustBe Some("testData")
    }

    "return none" in new Setup {
      when(mockStore.getData(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.failed(new SessionKeyNotFoundException("")))

      val result = Await.result(testService.getByKey("sessionID", "testKey"), 5.seconds)
      result mustBe None
    }
  }

  "updateDataKey" should {
    "return an UpdateWriteResult" when {
      "given a sessionID, a key and data" in new Setup {
        when(mockStore.updateSession(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(MongoSuccessUpdate))

        val result = Await.result(testService.updateDataKey("testID", testUpdateSet), 5.seconds)
        result mustBe MongoSuccessUpdate
      }
    }
  }

  "destroySessionRecord" should {
    "remove a session record" when {
      "given a valid session id" in new Setup {
        when(mockStore.removeSession(ArgumentMatchers.any()))
          .thenReturn(Future.successful(MongoSuccessDelete))

        val result = testService.destroySessionRecord("sessionID")
        Await.result(result, 5.seconds) mustBe true
      }
    }

    "return a MongoFailedDelete" when {
      "there was a problem deleting the session record" in new Setup {
        when(mockStore.removeSession(ArgumentMatchers.any()))
          .thenReturn(Future.successful(MongoFailedDelete))

        val result = testService.destroySessionRecord("sessionID")
        Await.result(result, 5.seconds) mustBe false
      }
    }
  }
}
