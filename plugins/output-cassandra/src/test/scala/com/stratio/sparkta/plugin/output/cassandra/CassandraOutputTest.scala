/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.output.cassandra

import com.datastax.spark.connector.cql.CassandraConnector
import com.stratio.sparkta.sdk.{TableSchema, TypeOp, WriteOp}
import org.apache.spark.SparkConf
import org.apache.spark.sql._
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class CassandraOutputTest extends FlatSpec with Matchers with MockitoSugar with AnswerSugar {

  val s = "sum"
  val operation = Some(Map(s ->(WriteOp.Inc, TypeOp.Int)))
  val properties = Map(("connectionHost", "127.0.0.1"), ("connectionPort", "9042"))

  "getSparkConfiguration" should "return a Seq with the configuration" in {
    val configuration = Map(("connectionHost", "127.0.0.1"), ("connectionPort", "9042"))
    val cass = CassandraOutput.getSparkConfiguration(configuration)

    cass should be(List(("spark.cassandra.connection.host", "127.0.0.1"), ("spark.cassandra.connection.port", "9042")))
  }

  "doPersist" should "return nothing because DataFramWriter are imposible to mock since it is a final class" in {

    val tableSchema = Seq(TableSchema("outputName", "dim1", StructType(Array(
      StructField("dim1", StringType, false))), "minute"))

    val out =  spy(new CassandraOutput("key", None, properties, operation, Option(tableSchema)))
    val df: DataFrame = mock[DataFrame]

    doNothing().when(out).write(df,"tablename")
    out.upsert(df,"tablename","minute")
  }

  "setup" should "return X" in {

    val tableSchema = Seq(TableSchema("outputName", "dim1", StructType(Array(
      StructField("dim1", StringType, false))), "minute"))

    val cassandraConnector: CassandraConnector = mock[CassandraConnector]

    val out =  new CassandraOutput("key", Some(1), properties, operation, Option(tableSchema)) {
      override val textIndexFields = Option(Array("test"))
      override def getCassandraConnector(): CassandraConnector = {
        cassandraConnector
      }
    }
    out.setup
  }
}
