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
package app

import com.cjwwdev.security.encryption.DataSecurity
import models.UpdateSet
import utils.CJWWIntegrationUtils
import play.api.test.Helpers._

class UpdateSessionISpec extends CJWWIntegrationUtils {
  "/session/:sessionId" should {
    "return an Ok" when {
      "the session has been updated" in {
        beforeITest()

        val enc = DataSecurity.encryptData[UpdateSet](UpdateSet("testKey", "SomeData")).get

        val request = client(s"$baseUrl/session/session-test-session-id")
          .withHeaders(
            CONTENT_TYPE -> TEXT,
            "appId" -> "abda73f4-9d52-4bb8-b20d-b5fffd0cc130"
          )
          .withBody(enc)
          .put(enc)

        val result = await(request)

        result.status mustBe OK

        await(sessionRepo.store.getData("session-test-session-id", "testKey")) mustBe "SomeData"

        afterITest()
      }
    }

    "return a forbidden" when {
      "the requested session cannot be found" in {
        val enc = DataSecurity.encryptData[UpdateSet](UpdateSet("testKey", "SomeData")).get

        val request = client(s"$baseUrl/session/session-test-session-id")
          .withHeaders(
            CONTENT_TYPE -> TEXT,
            "appId" -> "abda73f4-9d52-4bb8-b20d-b5fffd0cc130"
          )
          .withBody(enc)
          .put(enc)

        val result = await(request)

        result.status mustBe FORBIDDEN
      }

      "the request is not authorised" in {
        val enc = DataSecurity.encryptData[UpdateSet](UpdateSet("testKey", "SomeData")).get

        val request = client(s"$baseUrl/session/session-test-session-id")
          .withHeaders(
            CONTENT_TYPE -> TEXT
          )
          .withBody(enc)
          .put(enc)

        val result = await(request)

        result.status mustBe FORBIDDEN
      }
    }
  }
}
