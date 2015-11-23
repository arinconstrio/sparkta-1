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

package com.stratio.sparkta.sdk

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.sdk.ValidatingPropertyMap._

@RunWith(classOf[JUnitRunner])
class ValidatingPropertyMapTest extends FlatSpec with ShouldMatchers {

  trait ValuesMap {

    val theString = "Sparkta is awesome!"
    val one = 1
    val zero = 0
    val two = 2
    val oneString = "1"
    val theLong = 2L
    val theDouble = 2D
    val trueString = "true"
    val falseString = "false"
    val trueBool = true
    val data: Map[String, JSerializable] = Map("someString" -> theString, "someInt" -> one, "someLong" -> theLong,
      "someTrue" -> trueString, "someFalse" -> falseString, "zero" -> zero, "two" -> two,
      "someBoolean" -> trueBool, "oneString" -> oneString, "theDouble" -> theDouble)
  }

  "ValidatingProperty" should " returs value as String" in new ValuesMap {
    data.getString("someString") should be(theString)
    data.getString("someInt") should be(one.toString)
    data.getString("someInt", "default") should be(one.toString)
    data.getString("dummy", "default") should be("default")
    an[IllegalStateException] should be thrownBy data.getString("otherLong")
  }

  it should "returs value as Option" in new ValuesMap {
    data.getString("someInt", None) should be(Some(one.toString))
    data.getString("dummy", None) should be(None)
    data.getString("dummy", Some("dummy")) should be(Some("dummy"))
  }

  it should "returs value as Boolean" in new ValuesMap {
    data.getBoolean("someTrue") should be(true)
    data.getBoolean("someFalse") should be(false)
    data.getBoolean("someInt") should be(true)
    data.getBoolean("zero") should be(false)
    data.getBoolean("someBoolean") should be(true)
    an[Exception] should be thrownBy data.getBoolean("dummy")
    an[IllegalStateException] should be thrownBy data.getBoolean("two")
  }

  it should "returs value as Int" in new ValuesMap {
    data.getInt("oneString") should be(one)
    an[IllegalStateException] should be thrownBy data.getInt("theString")
    an[IllegalStateException] should be thrownBy data.getInt("someString")
    data.getInt("someInt") should be(one)
    data.getInt("someLong") should be(two)
    an[IllegalStateException] should be thrownBy data.getInt("theDouble")
  }

  it should "returs value as map" in new ValuesMap {
    data.getMap("one") should be(Some(Map("String" -> oneString)))
    data.getMap("dummy") should be(None)
  }

  it should "check key" in new ValuesMap {
    data.hasKey("someBoolean") should be(true)
    data.hasKey("dummy") should be(false)
  }

}
