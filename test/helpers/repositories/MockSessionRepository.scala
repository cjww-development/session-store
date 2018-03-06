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

package helpers.repositories

import com.cjwwdev.mongo.responses._
import helpers.other.{Fixtures, TestDataGenerator}
import models.Session
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.play.PlaySpec
import repositories.SessionRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MockSessionRepository extends BeforeAndAfterEach with MockitoSugar with Fixtures {
  self: PlaySpec =>

  val mockSessionRepository = mock[SessionRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  def mockCacheDataRepository(success: Boolean): OngoingStubbing[Future[MongoCreateResponse]] = {
    when(mockSessionRepository.cacheData(any(), any()))
      .thenReturn(Future(if(success) MongoSuccessCreate else MongoFailedCreate))
  }

  def mockGetSession(session: Option[Session]): OngoingStubbing[Future[Option[Session]]] = {
    when(mockSessionRepository.getSession(any())(any()))
      .thenReturn(Future(session))
  }

  def mockRenewSession(success: Boolean): OngoingStubbing[Future[MongoUpdatedResponse]] = {
    when(mockSessionRepository.renewSession(any())(any()))
      .thenReturn(Future(if(success) MongoSuccessUpdate else MongoFailedUpdate))
  }

  def mockGetSessions(sessions: List[Session]): OngoingStubbing[Future[List[Session]]] = {
    when(mockSessionRepository.getSessions(any()))
      .thenReturn(Future(sessions))
  }

  def mockUpdateSession(success: Boolean): OngoingStubbing[Future[MongoUpdatedResponse]] = {
    when(mockSessionRepository.updateSession(any(), any())(any()))
      .thenReturn(Future(if(success) MongoSuccessUpdate else MongoFailedUpdate))
  }

  def mockRemoveSession(success: Boolean): OngoingStubbing[Future[MongoDeleteResponse]] = {
    when(mockSessionRepository.removeSession(any()))
      .thenReturn(Future(if(success) MongoSuccessDelete else MongoFailedDelete))
  }

  def mockValidateSession(validated: Boolean): OngoingStubbing[Future[Boolean]] = {
    when(mockSessionRepository.validateSession(any()))
      .thenReturn(Future(validated))
  }
}
