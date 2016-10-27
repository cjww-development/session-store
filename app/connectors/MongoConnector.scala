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

import config.MongoConfiguration
import play.api.libs.json.OFormat
import reactivemongo.api.MongoDriver
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object MongoConnector extends MongoConnector {
  // $COVERAGE-OFF$
  val driver = new MongoDriver
  val connection = driver.connection(List("localhost"))
  val database = connection.database("cjww-industries")

  def collection(name : String) : Future[JSONCollection] = {
    database.map {
      _.collection(name)
    }
  }
  // $COVERAGE-ON$
}

trait MongoConnector extends MongoConfiguration {

  def create[T](collectionName : String, data : T)(implicit format : OFormat[T]) : Future[WriteResult] = {
    collection(collectionName) flatMap {
      _.insert[T](data)
    }
  }

  def read[T](collectionName : String, query : BSONDocument)(implicit format : OFormat[T]) : Future[Option[T]] = {
    collection(collectionName).flatMap {
      _.find(query).one[T]
    }
  }

  def update[T](collectionName : String, selectedData : BSONDocument, data : T)(implicit format : OFormat[T]) : Future[UpdateWriteResult] = {
    collection(collectionName).flatMap {
      _.update(selectedData, data)
    }
  }

  def delete[T](collectionName : String, query : BSONDocument) : Future[WriteResult] = {
    collection(collectionName).flatMap {
      _.remove(query)
    }
  }
}
