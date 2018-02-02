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
package builders

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import models.Session
import play.api.mvc.{Action, Result}
import play.api.test.FakeRequest
import repositories.SessionRepository
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Future

object AuthBuilder {

  implicit val system       = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val ws                    = AhcWSClient()

  def actionWithUser[T](action: Action[T], request: FakeRequest[_], mockRepo: SessionRepository)
                       (validateResponse: Boolean, getSessionResponse: Option[Session])
                       (testAction: Future[Result] => Any): Any = {
    when(mockRepo.validateSession(ArgumentMatchers.any()))
      .thenReturn(Future.successful(validateResponse))

    when(mockRepo.getSession(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(getSessionResponse))

    val result = action.apply(request)
    testAction(result.run())
  }

  def updateActionWithUser(action: Action[String], request: FakeRequest[String], mockRepo: SessionRepository)
                          (validateResponse: Boolean, getSessionResponse: Option[Session])
                          (testAction: Future[Result] => Any): Any = {
    when(mockRepo.validateSession(ArgumentMatchers.any()))
      .thenReturn(Future.successful(validateResponse))

    when(mockRepo.getSession(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(getSessionResponse))

    val result = action.apply(request)
    testAction(result)
  }
}
