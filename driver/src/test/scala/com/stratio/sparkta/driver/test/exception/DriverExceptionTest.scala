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

package com.stratio.sparkta.driver.test.exception

import com.stratio.sparkta.driver.exception.DriverException
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DriverExceptionTest extends FlatSpec with ShouldMatchers {

  "DriverException" should "return a Throwable" in {

    val msg = "my custom exception"

    val ex = DriverException.create(msg)

    ex.getMessage should be(msg)
  }
  it should "return a exception with the msg and a cause" in {

    val msg = "my custom exception"

    val ex = DriverException.create(msg, new RuntimeException("cause"))

    ex.getCause.getMessage should be("cause")
  }

}
