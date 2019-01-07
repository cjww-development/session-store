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
import common.{NoMatchingSession, Response}
import models.Session
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import services.SessionService

import scala.concurrent.Future

trait MockSessionService extends BeforeAndAfterEach with MockitoSugar {
  self: PlaySpec =>

  val mockSessionService = mock[SessionService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionService)
  }

  def mockCacheData(session: Option[Session], exception: Option[Exception] = None): OngoingStubbing[Future[Option[Session]]] = {
    when(mockSessionService.cacheData(any())(any(), any()))
      .thenReturn(
        if(exception.isDefined) {
          Future.failed(exception.get)
        } else {
          Future.successful(session)
        }
      )
  }

  def mockGetKey(sessionExists: Boolean, keyExists: Boolean): OngoingStubbing[Future[Either[Option[String], Response]]] = {
    when(mockSessionService.getByKey(any(), any())(any(), any()))
      .thenReturn(if(sessionExists) {
        if(keyExists) {
          Future.successful(Left(Some("testData")))
        } else {
          Future.successful(Left(None))
        }
      } else {
        Future.successful(Right(NoMatchingSession))
      })
  }

  def mockGetServiceSession(session: Option[Session]): OngoingStubbing[Future[Option[Session]]] = {
    when(mockSessionService.getSession(any())(any(), any()))
      .thenReturn(Future.successful(session))
  }

  def mockUpdateDataKey(updateFailed: Boolean): OngoingStubbing[Future[Seq[(String, String)]]] = {
    when(mockSessionService.updateDataKey(any(), any())(any(), any()))
      .thenReturn(
        Future.successful(if(updateFailed) Seq("key" -> MongoFailedUpdate.toString) else Seq("key" -> MongoSuccessUpdate.toString))
      )
  }

  def mockDestroySessionRecord(destroyed: Boolean): OngoingStubbing[Future[Boolean]] = {
    when(mockSessionService.destroySessionRecord(any())(any(), any()))
      .thenReturn(Future.successful(destroyed))
  }

  def mockCleanseSessions: OngoingStubbing[Future[MongoDeleteResponse]] = {
    when(mockSessionService.cleanseSessions(any()))
      .thenReturn(Future.successful(MongoSuccessDelete))
  }
}
