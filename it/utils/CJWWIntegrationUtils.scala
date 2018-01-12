/*
 * Copyright 2018 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package utils

import com.cjwwdev.test.data.TestDataGenerator
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.duration._
import scala.concurrent.{Await, Awaitable}

trait CJWWIntegrationUtils extends PlaySpec with GuiceOneServerPerSuite with TestDataGenerator {

  val additionalConfiguration = Map(
    "repositories.SessionRepositoryImpl.database"   -> "test-session-db",
    "repositories.SessionRepositoryImpl.collection" -> "test-session-collection"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(additionalConfiguration)
    .build()

  val sessionRepo: SessionRepository = app.injector.instanceOf(classOf[SessionRepository])

  val baseUrl = s"http://localhost:$port/session-store"

  lazy val ws = app.injector.instanceOf(classOf[WSClient])

  def client(url: String): WSRequest = ws.url(url)

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, 5.seconds)

  private val request = FakeRequest()

  def beforeITest(): Unit = await(sessionRepo.cacheData(s"session-$uuid", "testData"))

  def afterITest(): Unit = await(sessionRepo.removeSession(s"session-$uuid"))
}
