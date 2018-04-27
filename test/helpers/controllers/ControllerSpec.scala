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

import java.time.LocalDateTime

import com.cjwwdev.implicits.ImplicitJsValues._
import com.cjwwdev.testing.common.FutureHelpers
import helpers.auth.AuthBuilder
import helpers.other.Fixtures
import helpers.services.MockSessionService
import org.scalatest.Assertion
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http._
import play.api.libs.json.JsValue
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
    with MockSessionService
    with AuthBuilder
    with HttpVerbs {

  implicit val ec: ExecutionContext = global.prepare()

  def assertFutureResult(result: => Future[Result])(assertions: Future[Result] => Assertion): Assertion = {
    assertions(result)
  }

  def evaluateJsonResponse(method: String, status: Int, mainBody: JsValue)(notError: Boolean)(responseBody: JsValue): Assertion = {
    responseBody.get[String]("method") mustBe method.toUpperCase
    responseBody.get[Int]("status")    mustBe status
    if(notError) {
      responseBody.get[JsValue]("body") mustBe mainBody
    } else {
      responseBody.get[JsValue]("errorMessage") mustBe mainBody
    }
    val dateTime = LocalDateTime.parse(responseBody.getFirstMatch[String]("requestCompletedAt"))
    dateTime.getYear       mustBe LocalDateTime.now.getYear
    dateTime.getMonthValue mustBe LocalDateTime.now.getMonthValue
    dateTime.getDayOfMonth mustBe LocalDateTime.now.getDayOfMonth
  }
}
