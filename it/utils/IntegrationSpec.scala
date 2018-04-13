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

package utils

import com.cjwwdev.implicits.ImplicitHandlers
import com.cjwwdev.testing.integration.IntegrationTestSpec
import com.cjwwdev.testing.integration.application.IntegrationApplication
import play.api.libs.ws.WSRequest
import repositories.SessionRepository

trait IntegrationSpec extends IntegrationTestSpec with TestDataGenerator with ImplicitHandlers with IntegrationApplication {

  override val currentAppBaseUrl = "session-store"

  override val appConfig = Map(
    "repositories.SessionRepositoryImpl.database"   -> "test-session-db",
    "repositories.SessionRepositoryImpl.collection" -> "test-session-collection",
    "jobs.session-cleaner.enabled"                  -> false
  )

  def client(url: String): WSRequest = ws.url(url)

  val sessionId = generateTestSystemId(SESSION)
  val contextId = generateTestSystemId(CONTEXT)

  val sessionRepo: SessionRepository = app.injector.instanceOf(classOf[SessionRepository])

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    await(sessionRepo.collection.flatMap(_.drop(failIfNotFound = false)))
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    await(sessionRepo.cacheData(sessionId))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    await(sessionRepo.collection.flatMap(_.drop(failIfNotFound = false)))
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    await(sessionRepo.collection.flatMap(_.drop(failIfNotFound = false)))
  }
}
