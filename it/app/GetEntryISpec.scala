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
import play.api.libs.json.{JsString, Json}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import utils.IntegrationSpec

class GetEntryISpec extends IntegrationSpec {

  s"/session/$sessionId/data?key=contextId" should {
    "return an Ok with body" when {
      "data has been found with the key" in {
        await(sessionRepo.collection.flatMap(
          _.update(BSONDocument("sessionId" -> generateTestSystemId(SESSION)), BSONDocument("$set" -> BSONDocument("data.contextId" -> contextId)))
        ))

        val request = client(s"$testAppUrl/session/$sessionId/data?key=contextId")
          .withHttpHeaders(
            "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", Some(sessionId)).encrypt,
            CONTENT_TYPE   -> TEXT
          ).get()

        val result = await(request)
        result.status mustBe OK
        evaluateJsonResponse(
          GET,
          OK,
          sessionId,
          JsString(contextId)
        )(notError = true)(Json.parse(result.body))
      }
    }

    "return a NoContent" when {
      "no data has been found against the key" in {
        val request = client(s"$testAppUrl/session/$sessionId/data?key=invalid-key")
          .withHttpHeaders(
            "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", Some(sessionId)).encrypt,
            CONTENT_TYPE   -> TEXT
          ).get()

        val result = await(request)
        result.status mustBe NO_CONTENT
      }
    }
  }
}
