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

package app

import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.implicits.ImplicitDataSecurity._
import utils.IntegrationSpec

class CacheDataISpec extends IntegrationSpec {
  s"/session/$sessionId/ (initialise)" should {
    "return a created" when {
      "a new session has been created in session-store" in {
        val request = client(s"$testAppUrl/session/$sessionId")
          .withHttpHeaders(
            "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", Some(sessionId)).encrypt,
            CONTENT_TYPE   -> TEXT
          ).post("")

        val result = await(request)
        result.status mustBe CREATED

        val userInfo = await(sessionRepo.getSession(sessionId))
        userInfo.get.data mustBe Map.empty[String, String]
      }
    }

    "return a forbidden" when {
      "the request is not authorised" in {
        val result = await(client(s"$testAppUrl/session/$sessionId").post(""))
        result.status mustBe FORBIDDEN
      }
    }
  }
}
