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
import play.api.libs.json.JsValue
import play.api.test.Helpers._
import utils.CJWWIntegrationUtils

class FetchContextIdISpec extends CJWWIntegrationUtils {
  val testSessionId = generateTestSystemId(SESSION)

  s"/session/$testSessionId/context" should {
    "return an OK" when {
      "the context id has been retrieved" in {
        beforeITest(testSessionId)

        val request = await(client(s"$baseUrl/session/$testSessionId/context")
          .withHeaders(
            "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testSessionId).encryptType,
            CONTENT_TYPE   -> TEXT
          ).get()
        )

        request.status mustBe OK
        request.body.decrypt mustBe "testData"

        afterITest(testSessionId)
      }
    }
  }
}
