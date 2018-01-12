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

package app

import com.cjwwdev.security.encryption.DataSecurity
import play.api.libs.json.JsValue
import play.api.test.Helpers._
import utils.CJWWIntegrationUtils

class FetchContextIdISpec extends CJWWIntegrationUtils {
  "/session/:sessionId/context" should {
    "return an OK" when {
      "the context id has been retrieved" in {
        beforeITest()

        val request = await(client(s"$baseUrl/session/session-$uuid/context")
          .withHeaders(
            "appId"      -> "abda73f4-9d52-4bb8-b20d-b5fffd0cc130",
            CONTENT_TYPE -> TEXT,
            "cookieId"   -> s"session-$uuid"
          ).get())

        request.status mustBe OK
        DataSecurity.decryptIntoType[JsValue](request.body).get.\("contextId").as[String] mustBe "testData"

        afterITest()
      }
    }
  }
}
