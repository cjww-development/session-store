/*
 * Copyright 2020 CJWW Development
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
 */
package models

import helpers.models.ModelSpec
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.Json

class SessionSpec extends ModelSpec {

  val dateFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  val dateTime: DateTime            = dateFormat.parseDateTime("2017-04-13 19:59:14")

  val testSession = Session(
    sessionId       = "testSessionId",
    data            = Map("testKey" -> "testData"),
    modifiedDetails = SessionTimestamps(
      created      = dateTime,
      lastModified = dateTime
    )
  )

  val testJson = Json.parse(
    """
      |{
      | "sessionId" : "testSessionId",
      | "data" : {
      |   "testKey" : "testData"
      | },
      | "modifiedDetails" : {
      |   "created" : {
      |     "$date" : 1492109954000
      |   },
      |   "lastModified" : {
      |     "$date" : 1492109954000
      |   }
      | }
      |}
    """.stripMargin)

  "Session" should {
    "be able to be transformed from a case class to json" in {
      Json.toJson(testSession) mustBe testJson
    }

    "be able to be transformed from Json into a case class" in {
      Json.fromJson[Session](testJson).mustNotHaveErrors
    }
  }
}
