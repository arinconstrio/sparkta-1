/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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

package com.stratio.sparkta.sdk

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class HostPortConfSpec extends WordSpec with Matchers {

  "getHostPortConf" should {

    "return a Seq of tuples (host,port) format" in {

      val conn = """[{"node":"localhost","defaultPort":"9200"}]"""
      val validating: ValidatingPropertyMap[String, JsoneyString] =
        new ValidatingPropertyMap[String, JsoneyString](Map("nodes" -> JsoneyString(conn)))
      validating.getHostPortConfs("nodes", "localhost", "9200") should be(List(("localhost", 9200)))
    }

    "return a tuple with a default port specified in the function (host,port) format" in {

      val conn = """[{"node":"localhost"}]"""
      val defaultPort: String = "9200"
      val validating: ValidatingPropertyMap[String, JsoneyString] =
        new ValidatingPropertyMap[String, JsoneyString](Map("nodes" -> JsoneyString(conn)))
      validating.getHostPortConfs("nodes", "localhost", defaultPort) should be(List(("localhost", 9200)))
    }
  }

  "return a Seq of tuples with a default port specified in the function (host,port) format" in {

    val conn = """[{"node":"localhost"},{"node":"localhost"},{"node":"localhost"}]"""
    val defaultPort: String = "9200"
    val validating: ValidatingPropertyMap[String, JsoneyString] =
      new ValidatingPropertyMap[String, JsoneyString](Map("nodes" -> JsoneyString(conn)))
    validating.getHostPortConfs("nodes", "localhost", defaultPort) should be
    (List(("localhost", 9200), ("localhost", 9200), ("localhost", 9200)))
  }

  "return a tuple with a default host specified in the function (host,port) format" in {

    val conn = """[{"defaultPort":"9200"}]"""
    val defaultHost: String = "localhost"
    val validating: ValidatingPropertyMap[String, JsoneyString] =
      new ValidatingPropertyMap[String, JsoneyString](Map("nodes" -> JsoneyString(conn)))
    validating.getHostPortConfs("nodes", defaultHost, "9200") should be(List(("localhost", 9200)))
  }

  "return a Seq of tuples with a default host specified in the function (host,port) format" in {

    val conn = """[{"defaultPort":"9200"},{"defaultPort":"9200"},{"defaultPort":"9200"}]"""
    val defaultHost: String = "localhost"
    val validating: ValidatingPropertyMap[String, JsoneyString] =
      new ValidatingPropertyMap[String, JsoneyString](Map("nodes" -> JsoneyString(conn)))
    validating.getHostPortConfs("nodes", defaultHost, "9200") should be
    (List(("localhost", 9200), ("localhost", 9200), ("localhost", 9200)))
  }
}