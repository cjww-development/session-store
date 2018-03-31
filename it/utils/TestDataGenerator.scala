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

import java.util.UUID

import org.joda.time.{DateTime, DateTimeZone}

import scala.util.Random.nextString

trait TestDataGenerator {
  val USER      = "user"
  val ORG       = "org-user"
  val CONTEXT   = "context"
  val SESSION   = "session"
  val DEVERSITY = "deversity"
  val DIAG      = "diag"
  val HUB       = "hub"
  val FEED_ITEM = "feed-item"

  val uuid = UUID.randomUUID()

  val now = DateTime.now(DateTimeZone.UTC)

  val createTestUserName: String = nextString(10)

  val createTestEmail: String = s"${nextString(3)}@${nextString(5)}.com"

  def generateTestSystemId(prefix: String): String = s"$prefix-$uuid"
}
