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

package com.stratio.sparkta.sdk.test

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.{Event, Parser}

class ParserMock(name: String,
                 order: Integer,
                 inputField: String,
                 outputFields: Seq[String],
                 properties: Map[String, JSerializable])
  extends Parser(name, order, inputField, outputFields, properties) {

  override def parse(data: Event): Event = data
}
