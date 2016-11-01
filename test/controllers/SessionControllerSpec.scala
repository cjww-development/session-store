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

package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.ws.ahc.AhcWSClient
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SessionControllerSpec extends PlaySpec with OneAppPerSuite {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val ws = AhcWSClient()

  class Setup {
    val testController = new SessionController
  }

  "SessionController" should {
    "return a forbidden" when {
      "calling cache without an appID" in new Setup {
        val result = testController.cache()(FakeRequest().withHeaders(CONTENT_TYPE -> TEXT))
        status(result.run()) mustBe FORBIDDEN
      }
    }
  }
}
