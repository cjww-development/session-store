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

import com.cjwwdev.mongo.{MongoSuccessUpdate, _}
import mocks.MongoMocks
import models.InitialSession
import org.joda.time.DateTime
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import org.mockito.Mockito._
import org.mockito.{ArgumentMatchers => Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class SessionRepositorySpec extends PlaySpec with OneAppPerSuite with MockitoSugar with MongoMocks {

  val mockConnector = mock[MongoConnector]

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
    val testRepo = new SessionRepository(mockConnector)
  }

  "cacheData" should {
    "return a successful WriteResult" when {
      "given a session id and data" in new Setup {
        when(mockConnector.create[InitialSession](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(MongoSuccessCreate))

        val result = Await.result(testRepo.cacheData("sessionID", "testData"), 5.seconds)
        result mustBe MongoSuccessCreate
      }
    }
  }

  "getData" should {
    "return an optional string" when {
      "given a sessionID and a key" in new Setup {
        when(mockConnector.read[InitialSession](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(MongoSuccessRead(testInitial)))

        val result = Await.result(testRepo.getData("testID", "testKey"), 5.seconds)
        result mustBe Some("testData")
      }
    }

    "return None" when {
      "given a sessionID and a key" in new Setup {
        when(mockConnector.read[InitialSession](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(MongoFailedRead))

        val result = Await.result(testRepo.getData("testID", "testKey"), 5.seconds)
        result mustBe None
      }
    }
  }

  "removeSessionRecord" should {
    "return a successful WriteResult" when {
      "given a session id" in new Setup {
        when(mockConnector.delete(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(MongoSuccessDelete))

        val result = Await.result(testRepo.removeSessionRecord("sessionID"), 5.seconds)
        result mustBe MongoSuccessDelete
      }
    }
  }

  "getSession" should {
    "return an optional initial session" when {
      "given a sessionID" in new Setup {
        when(mockConnector.read[InitialSession](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(MongoSuccessRead(testInitial)))

        val result = Await.result(testRepo.getSession("testID"), 5.seconds)
        result mustBe Some(testInitial)
      }
    }

    "return None" when {
      "given a sessionID" in new Setup {
        when(mockConnector.read[InitialSession](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(MongoFailedRead))

        val result = Await.result(testRepo.getSession("testID"), 5.seconds)
        result mustBe None
      }
    }
  }

  "updateSession" should {
    "amend a key" when {
      "given a new set of data" in new Setup {
        when(mockConnector.update[InitialSession](Matchers.any(),Matchers.any(),Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(MongoSuccessUpdate))

        val result = Await.result(testRepo.updateSession("",testInitial,"",""), 5.seconds)
        result mustBe MongoSuccessUpdate
      }
    }
  }
}
