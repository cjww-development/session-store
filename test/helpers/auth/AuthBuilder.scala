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
package helpers.auth

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import helpers.repositories.MockSessionRepository
import models.Session
import org.scalatest.Assertion
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.JsValue
import play.api.libs.ws.ahc.AhcWSClient
import play.api.mvc.{Action, Result}
import play.api.test.FakeRequest

import scala.concurrent.Future

trait AuthBuilder extends PlaySpec with MockSessionRepository with MockitoSugar {

  implicit val system = ActorSystem()
  implicit val materialiser = ActorMaterializer()
  val ws = AhcWSClient()

  def runActionWithAuth[A, B](action: Action[A], request: FakeRequest[B], session: Option[Session])(testAction: Future[Result] => Assertion): Assertion = {
    mockValidateSession(true)
    mockGetSession(session)
    testAction(action(request).run())
  }

  def runActionWithAuthStringBody(action: Action[String], request: FakeRequest[String], session: Option[Session])
                                 (testAction: Future[Result] => Assertion): Assertion = {
    mockValidateSession(true)
    mockGetSession(session)
    testAction(action(request))
  }

  def runActionWithAuthJsonBody(action: Action[JsValue], request: FakeRequest[JsValue], session: Option[Session])
                               (testAction: Future[Result] => Assertion): Assertion = {
    mockValidateSession(true)
    mockGetSession(session)
    testAction(action(request))
  }
}
