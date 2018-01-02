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
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import utils.CJWWIntegrationUtils

class CacheDataISpec extends CJWWIntegrationUtils {
  "/session/{session-test-session-id}/cache" should {
    "return a created" when {
      "a new session has been created in session-store" in {
        val request = client(s"$baseUrl/session/session-$uuid/cache")
          .withHeaders("appId" -> "abda73f4-9d52-4bb8-b20d-b5fffd0cc130")
          .post(DataSecurity.encryptType[JsValue](Json.parse("""{"contextId" : "testContextId"}""")))

        val result = await(request)
        result.status mustBe CREATED

        val userInfo = await(sessionRepo.getSession(s"session-$uuid"))
        DataSecurity.decryptString(userInfo.get.data("contextId")) mustBe "testContextId"

        afterITest()
      }
    }

    "return a forbidden" when {
      "the request is not authorised" in {
        val result = await(client(s"$baseUrl/session/session-$uuid/cache").post(""))
        result.status mustBe FORBIDDEN
      }
    }
  }
}
