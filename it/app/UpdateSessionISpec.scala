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
 */

package app

import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.security.encryption.DataSecurity
import models.UpdateSet
import utils.CJWWIntegrationUtils
import play.api.test.Helpers._

class UpdateSessionISpec extends CJWWIntegrationUtils {
  val testSessionId = generateTestSystemId(SESSION)

  s"/session/$testSessionId" should {
    "return an Ok" when {
      "the session has been updated" in {
        beforeITest(testSessionId)

        val enc = DataSecurity.encryptType[UpdateSet](UpdateSet("testKey", "SomeData"))

        val request = client(s"$baseUrl/session/$testSessionId")
          .withHeaders(
            "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
            CONTENT_TYPE   -> TEXT
          )
          .withBody(enc)
          .put(enc)

        val result = await(request)

        result.status mustBe CREATED

        await(sessionRepo.getSession(testSessionId)).get.data("testKey") mustBe "SomeData"

        afterITest(testSessionId)
      }
    }

    "return a forbidden" when {
      "the requested session cannot be found" in {
        val enc = DataSecurity.encryptType[UpdateSet](UpdateSet("testKey", "SomeData"))

        val request = client(s"$baseUrl/session/$testSessionId")
          .withHeaders(
            "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
            CONTENT_TYPE   -> TEXT
          )
          .withBody(enc)
          .put(enc)

        val result = await(request)

        result.status mustBe FORBIDDEN

        afterITest(testSessionId)
      }

      "the request is not authorised" in {
        val enc = DataSecurity.encryptType[UpdateSet](UpdateSet("testKey", "SomeData"))

        val request = client(s"$baseUrl/session/$testSessionId")
          .withHeaders(
            CONTENT_TYPE -> TEXT
          )
          .withBody(enc)
          .put(enc)

        val result = await(request)

        result.status mustBe FORBIDDEN

        afterITest(testSessionId)
      }
    }
  }
}
