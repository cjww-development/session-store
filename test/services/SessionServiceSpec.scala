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
package services

import mocks.MongoMocks
import models.InitialSession
import org.joda.time.DateTime
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import org.mockito.Mockito._
import org.mockito.Matchers
import repositories.SessionRepository

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class SessionServiceSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with MongoMocks {

  val mockRepo = mock[SessionRepository]

  val successWrite = mockWriteResult()
  val failedWrite = mockWriteResult(fails = true)

  val successUWR = mockUpdateWriteResult()

  val testInitial = InitialSession(
    "testID",
    Map(
      "testKey" -> "testData"
    ),
    Map(
      "created" -> new DateTime(),
      "lastModified" -> new DateTime()
    )
  )

  class Setup {
    object TestService extends SessionService {
      val sessionRepo = mockRepo
    }
  }

  "cacheData" should {
    "return false if data is successfully saved" in new Setup {
      when(mockRepo.cacheData(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(successWrite))

      val result = TestService.cacheData("sessionID", "testData")
      Await.result(result, 5.seconds) mustBe false
    }
  }

  "getByKey" should {
    "return an optional string" in new Setup {
      when(mockRepo.getData(Matchers.any(), Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Some("testData")))

      val result = Await.result(TestService.getByKey("sessionID", "testKey"), 5.seconds)
      result mustBe Some("testData")
    }
  }

  "updateDataKey" should {
    "return an UpdateWriteResult" when {
      "given a sessionID, a key and data" in new Setup {
        when(mockRepo.getSession(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(testInitial)))

        when(mockRepo.updateSession(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(successUWR))

        val result = Await.result(TestService.updateDataKey("testID","userInfo","testData"), 5.seconds)
        result mustBe successUWR
      }
    }
  }

  "destroySessionRecord" should {
    "remove a session record" when {
      "given a valid session id" in new Setup {
        when(mockRepo.removeSessionRecord(Matchers.any()))
          .thenReturn(Future.successful(successWrite))

        val result = TestService.destroySessionRecord("sessionID")
        Await.result(result, 5.seconds) mustBe false
      }
    }
  }
}
