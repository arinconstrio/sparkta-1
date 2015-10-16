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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import com.stratio.sparkta.sdk.test.TypeConversionsMock

@RunWith(classOf[JUnitRunner])
class TypeConversionsSpec extends WordSpec with Matchers {

  "TypeConversions" should {

    val typeConvesions = new TypeConversionsMock

    "typeOperation must be " in {
      val expected = TypeOp.Int
      val result = typeConvesions.defaultTypeOperation
      result should be(expected)
    }

    "operationProps must be " in {
      val expected = Map("typeOp" -> "string")
      val result = typeConvesions.operationProps
      result should be(expected)
    }

    "the operation type must be " in {
      val expected = Some(TypeOp.String)
      val result = typeConvesions.getTypeOperation
      result should be(expected)
    }

    "the detailed operation type must be " in {
      val expected = Some(TypeOp.String)
      val result = typeConvesions.getTypeOperation("string")
      result should be(expected)
    }

    "the precision type must be " in {
      val expected = Precision("precision", TypeOp.String, Map())
      val result = typeConvesions.getPrecision("precision", Some(TypeOp.String))
      result should be(expected)
    }
  }
}
