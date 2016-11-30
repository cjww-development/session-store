// Copyright (C) 2011-2012 the original author or authors.
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
package models

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{JsValue, Json, Reads, Writes, __}

case class InitialSession(_id : String,
                          data : Map[String, String],
                          modifiedDetails : Map[String, DateTime])

object InitialSession {
  implicit val dateTimeRead: Reads[DateTime] =
    (__ \ "$date").read[Long].map { dateTime =>
      new DateTime(dateTime, DateTimeZone.UTC)
    }

  implicit val dateTimeWrite: Writes[DateTime] = new Writes[DateTime] {
    def writes(dateTime: DateTime): JsValue = Json.obj(
      "$date" -> dateTime.getMillis
    )
  }

  implicit val format = Json.format[InitialSession]


  def getDateTime : DateTime = {
    val milliseconds = DateTime.now
    new DateTime(milliseconds.getMillis, DateTimeZone.UTC)
  }
}
