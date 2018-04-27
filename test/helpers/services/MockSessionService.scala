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
import models.Session
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import services.SessionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockSessionService extends BeforeAndAfterEach with MockitoSugar {
  self: PlaySpec =>

  val mockSessionService = mock[SessionService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionService)
  }

  def mockCacheData(session: Option[Session], exception: Option[Exception] = None): OngoingStubbing[Future[Option[Session]]] = {
    when(mockSessionService.cacheData(any()))
      .thenReturn(
        if(exception.isDefined) {
          Future.failed(exception.get)
        } else {
          Future(session)
        }
      )
  }

  def mockGetKey(sessionExists: Boolean, keyExists: Boolean): OngoingStubbing[Future[Option[String]]] = {
    when(mockSessionService.getByKey(any(), any())(any()))
      .thenReturn(if(sessionExists) {
        if(keyExists) {
          Future(Some("testData"))
        } else {
          Future(None)
        }
      } else {
        Future.failed(new MissingSessionException("No Session"))
      })
  }

  def mockGetServiceSession(session: Option[Session]): OngoingStubbing[Future[Option[Session]]] = {
    when(mockSessionService.getSession(any()))
      .thenReturn(Future(session))
  }

  def mockUpdateDataKey(updateFailed: Boolean): OngoingStubbing[Future[Seq[(String, String)]]] = {
    when(mockSessionService.updateDataKey(any(), any()))
      .thenReturn(Future(if(updateFailed) Seq("key" -> MongoFailedUpdate.toString) else Seq("key" -> MongoSuccessUpdate.toString)))
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
