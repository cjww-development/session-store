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

package helpers.controllers

import com.cjwwdev.testing.common.FutureHelpers
import helpers.auth.AuthBuilder
import helpers.other.Fixtures
import org.scalatest.Assertion
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.{HeaderNames, HttpProtocol, MimeTypes, Status}
import play.api.mvc.Result
import play.api.test._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

trait ControllerSpec
  extends PlaySpec
    with MockitoSugar
    with HeaderNames
    with Status
    with MimeTypes
    with HttpProtocol
    with ResultExtractors
    with Writeables
    with EssentialActionCaller
    with RouteInvokers
    with FutureHelpers
    with Fixtures
    with AuthBuilder {

  implicit val ec: ExecutionContext = global.prepare()

  def assertFutureResult(result: => Future[Result])(assertions: Future[Result] => Assertion): Assertion = {
    assertions(result)
  }
}