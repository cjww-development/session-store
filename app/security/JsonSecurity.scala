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

package security

import java.util
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

import play.api.libs.json.{Format, JsValue, Json}
import com.typesafe.config.ConfigFactory
import org.apache.commons.codec.binary.Base64

import scala.util.Try

object JsonSecurity extends JsonSecurity

trait JsonSecurity extends JsonCommon {

  def encryptModel[T](data : T)(implicit format: Format[T]) : Option[String] = {
    def scramble(json : JsValue) : Option[String] = {
      val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
      cipher.init(Cipher.ENCRYPT_MODE, keyToSpec)
      Some(Base64.encodeBase64String(cipher.doFinal(json.toString.getBytes("UTF-8"))))
    }

    scramble(Json.toJson(data))
  }

  def decryptInto[T](data : String)(implicit format: Format[T]) : Option[T] = {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, keyToSpec)
    val attempt = Try(cipher.doFinal(Base64.decodeBase64(data)))
    attempt.isSuccess match {
      case true =>
        val unlocked = new String(attempt.get)
        validate[T](unlocked)
      case false => None
    }
  }
}

trait JsonCommon {
  private val KEY = ConfigFactory.load.getString("cjww.auth.key")
  private val SALT = ConfigFactory.load.getString("cjww.auth.payload")
  private val LENGTH = 16

  def keyToSpec : SecretKeySpec = {
    var keyBytes : Array[Byte] = (SALT + KEY).getBytes("UTF-8")
    val sha : MessageDigest = MessageDigest.getInstance("SHA-1")
    keyBytes = sha.digest(keyBytes)
    keyBytes = util.Arrays.copyOf(keyBytes, LENGTH)
    new SecretKeySpec(keyBytes, "AES")
  }

  def validate[T](unlocked : String)(implicit format: Format[T]) : Option[T] = {
    Json.parse(unlocked).validate[T].fold(
      // $COVERAGE-OFF$
      errors => None,
      // $COVERAGE-ON$
      valid => Some(valid)
    )
  }
}
