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

package connectors

import mocks.MongoMocks
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MongoConnectorSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with MongoMocks {

  val mockDriver = mock[MongoDriver]
  val mockConnection = mock[MongoConnection]
  val mockDatabase = mock[Future[DefaultDB]]
  val mockCollection = mock[JSONCollection]
  val mockCollectionName = "TestCollection"

  val mockWriteResult : WriteResult = mockWriteResult()
  val mockUpdatedWriteResult : UpdateWriteResult = mockUpdateWriteResult()

  class Setup {
    object TestConnector extends MongoConnector {
      val driver = mockDriver
      val connection = mockConnection
      val database = mockDatabase
      def collection(name : String) : Future[JSONCollection] = Future.successful(mockCollection)
    }

    case class TestModel(string : String, int: Int, boolean: Boolean)
    implicit val format = Json.format[TestModel]
    val testData = TestModel("testString", 1234, boolean = true)
    val updatedData = TestModel("stringTest", 9876, boolean = false)
  }

  "MongoConnector" should {
    "insert a model into the collection" in new Setup {
      when(mockCollection.insert[TestModel](Matchers.eq(testData), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(mockWriteResult))

      val result = TestConnector.create[TestModel](mockCollectionName, testData)

      result.map {
        result => assert(!result.hasErrors)
      }
    }

    "fetch a model from the collection" in new Setup {
      val result = TestConnector.read[TestModel](mockCollectionName, BSONDocument("string" -> "testString"))

      result.map {
        result => assert(result.get == testData)
      }
    }

    "update a document" in new Setup {
      val result = TestConnector.update[TestModel](mockCollectionName, BSONDocument("string" -> "testString"), updatedData)

      result.map {
        result => assert(!result.hasErrors)
      }
    }

    "delete a document" in new Setup {
      val result = TestConnector.delete[TestModel](mockCollectionName, BSONDocument("string" -> "testString"))

      result.map {
        result => assert(!result.hasErrors)
      }
    }
  }
}
