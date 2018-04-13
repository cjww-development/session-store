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
import utils.IntegrationSpec

class DestroySessionISpec extends IntegrationSpec {
  s"/session/$sessionId/destroy" should {
    "return an OK" when {
      "the session has been removed" in {
        val request = client(s"$testAppUrl/session/$sessionId")
          .withHeaders(
            "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", sessionId).encryptType,
            CONTENT_TYPE   -> TEXT
          )
          .delete()
        val result = await(request)

        result.status mustBe NO_CONTENT

        await(sessionRepo.getSession(sessionId)) mustBe None
      }
    }

    "return forbidden" when {
      "the session cannot be found" in {
        await(sessionRepo.removeSession(sessionId))

        val request = client(s"$testAppUrl/session/$sessionId")
          .withHeaders(
            "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", sessionId).encryptType,
            CONTENT_TYPE   -> TEXT
          )
          .delete()
        val result = await(request)

        result.status mustBe FORBIDDEN
      }

      "the request is not authorised" in {
        val request = client(s"$testAppUrl/session/$sessionId")
          .withHeaders(
            CONTENT_TYPE -> TEXT
          )
          .delete()
        val result = await(request)

        result.status mustBe FORBIDDEN
      }
    }
  }
}
