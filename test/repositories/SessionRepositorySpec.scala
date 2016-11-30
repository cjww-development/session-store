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

package repositories

import connectors.MongoConnector
import mocks.MongoMocks
import models.InitialSession
import org.joda.time.DateTime
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import org.mockito.Mockito._
import org.mockito.Matchers

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class SessionRepositorySpec extends PlaySpec with OneAppPerSuite with MockitoSugar with MongoMocks {

  val mockConnector = mock[MongoConnector]

  val successWrite = mockWriteResult()
  val failedWrite = mockWriteResult(fails = true)

  val testInitial = InitialSession(
    "testID",
    Map(
      "testKey" -> "testData"
    ),
    Map(
      "" -> new DateTime(),
      "" -> new DateTime()
    )
  )

  class Setup {
    object TestRepo extends SessionRepository {
      val mongoConnector = mockConnector
    }
  }

  "cacheData" should {
    "return a successful WriteResult" when {
      "given a session id and data" in new Setup {
        when(mockConnector.create[InitialSession](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(successWrite))

        val result = TestRepo.cacheData("sessionID", "testData")
        Await.result(result, 5.seconds).hasErrors mustBe false
      }
    }
  }

  "getData" should {
    "return an optional string" when {
      "given a sessionID and a key" in new Setup {
        when(mockConnector.read[InitialSession](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(testInitial)))

        val result = Await.result(TestRepo.getData("testID", "testKey"), 5.seconds)
        result mustBe Some("testData")
      }
    }

    "return None" when {
      "given a sessionID and a key" in new Setup {
        when(mockConnector.read[InitialSession](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(None))

        val result = Await.result(TestRepo.getData("testID", "testKey"), 5.seconds)
        result mustBe None
      }
    }
  }

  "removeSessionRecord" should {
    "return a successful WriteResult" when {
      "given a session id" in new Setup {
        when(mockConnector.delete(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(successWrite))

        val result = TestRepo.removeSessionRecord("sessionID")
        Await.result(result, 5.seconds).hasErrors mustBe false
      }
    }
  }
}
