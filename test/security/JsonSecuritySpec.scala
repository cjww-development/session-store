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

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json

class JsonSecuritySpec extends PlaySpec {

  class Setup {
    object TestSec extends JsonSecurity

    case class TestModel(data : String)
    implicit val format = Json.format[TestModel]
    val testData = TestModel("TestString")
  }

  "Enc and Dec a model" should {
    "return the same as what was input" in new Setup {
      val enc = TestSec.encryptModel[TestModel](testData)
      val dec = TestSec.decryptInto[TestModel](enc.get)
      assert(dec.get == testData)
      assert(dec.get.data == testData.data)
    }

    "return none if the input cannot be decrypted" in new Setup {
      val dec = TestSec.decryptInto[TestModel]("invalidData")
      assert(dec.isEmpty)
    }
  }
}
