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

package helpers.services

import com.cjwwdev.mongo.responses._
import common.{MissingSessionException, SessionKeyNotFoundException}
import helpers.other.{Fixtures, TestDataGenerator}
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers.any
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.play.PlaySpec
import services.SessionService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MockSessionService extends BeforeAndAfterEach with MockitoSugar {
  self: PlaySpec =>

  val mockSessionService = mock[SessionService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionService)
  }

  def mockCacheData(cached: Boolean): OngoingStubbing[Future[Boolean]] = {
    when(mockSessionService.cacheData(any(), any()))
      .thenReturn(Future(cached))
  }

  def mockGetKey(sessionExists: Boolean, keyExists: Boolean): OngoingStubbing[Future[String]] = {
    when(mockSessionService.getByKey(any(), any())(any()))
      .thenReturn(if(sessionExists) {
        if(keyExists) {
          Future("testData")
        } else {
          Future.failed(new SessionKeyNotFoundException(""))
        }
      } else {
        Future.failed(new MissingSessionException("No Session"))
      })
  }

  def mockUpdateDataKey(updateFailed: Boolean): OngoingStubbing[Future[MongoUpdatedResponse]] = {
    when(mockSessionService.updateDataKey(any(), any()))
      .thenReturn(Future(if(updateFailed) MongoFailedUpdate else MongoSuccessUpdate))
  }

  def mockDestroySessionRecord(destroyed: Boolean): OngoingStubbing[Future[Boolean]] = {
    when(mockSessionService.destroySessionRecord(any()))
      .thenReturn(Future(destroyed))
  }

  def mockCleanseSessions: OngoingStubbing[Future[MongoDeleteResponse]] = {
    when(mockSessionService.cleanseSessions)
      .thenReturn(Future(MongoSuccessDelete))
  }
}
