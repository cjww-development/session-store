// Copyright (C) 2016-2017 the original author or authors.
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
package utils

import java.util.UUID

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.{WS, WSClient, WSRequest}
import repositories.SessionRepository

import scala.concurrent.{Await, Awaitable}
import scala.concurrent.duration._

trait CJWWIntegrationUtils extends PlaySpec with GuiceOneServerPerSuite {

  val sessionRepo: SessionRepository = new SessionRepository

  val uuid = UUID.randomUUID

  val baseUrl = s"http://localhost:$port/session-store"

  lazy val ws = app.injector.instanceOf(classOf[WSClient])

  def client(url: String): WSRequest = ws.url(url)

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, 5.seconds)

  def beforeITest(): Unit = await(sessionRepo.cacheData(s"session-$uuid", "testData"))

  def afterITest(): Unit = await(sessionRepo.removeSession(s"session-$uuid"))
}
