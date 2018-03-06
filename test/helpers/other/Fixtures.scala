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

package helpers.other

import com.cjwwdev.implicits.ImplicitHandlers
import models.{Session, SessionTimestamps}
import org.joda.time.DateTime

trait Fixtures extends ImplicitHandlers with TestDataGenerator {

  val testSessionId = generateTestSystemId(SESSION)
  val testContextId = generateTestSystemId(CONTEXT)

  val dateTime = DateTime.now

  val testSession = Session(
    sessionId = testSessionId,
    data      = Map("contextId" -> testContextId.encrypt),
    modifiedDetails = SessionTimestamps(
      created      = dateTime,
      lastModified = dateTime
    )
  )
}
