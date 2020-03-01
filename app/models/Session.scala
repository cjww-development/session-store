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

import com.cjwwdev.json.TimeFormat
import org.joda.time.{DateTime, DateTimeZone, Interval}
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SessionTimestamps(created: DateTime, lastModified: DateTime)

object SessionTimestamps extends TimeFormat {
  implicit val standardFormat: OFormat[SessionTimestamps] = (
    (__ \ "created").format(dateTimeRead)(dateTimeWrite) and
    (__ \ "lastModified").format(dateTimeRead)(dateTimeWrite)
  )(SessionTimestamps.apply, unlift(SessionTimestamps.unapply))
}


case class Session(sessionId : String,
                   data : Map[String, String],
                   modifiedDetails : SessionTimestamps) {

  def hasTimedOut: Boolean = new Interval(modifiedDetails.lastModified, DateTime.now).toDuration.getStandardHours >= 1
}

object Session {
  implicit val standardFormat: OFormat[Session] = (
    (__ \ "sessionId").format[String] and
    (__ \ "data").format[Map[String, String]] and
    (__ \ "modifiedDetails").format[SessionTimestamps](SessionTimestamps.standardFormat)
  )(Session.apply, unlift(Session.unapply))

  def getDateTime : DateTime = new DateTime(DateTime.now.getMillis, DateTimeZone.UTC)
}
