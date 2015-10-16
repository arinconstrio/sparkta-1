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

import com.stratio.sparkta.sdk._

abstract class BaseOperatorMoc(name: String, properties: Map[String, JSerializable])
  extends Operator(name, properties) {

  override val defaultTypeOperation = TypeOp.Long

  override val writeOperation = WriteOp.Inc

  override def processReduce(values: Iterable[Option[Any]]): Option[Long] = None
}

class OperatorMock(name: String, properties: Map[String, JSerializable])
  extends BaseOperatorMoc(name: String, properties: Map[String, JSerializable]) with ProcessMapAsNumber {

  override val castingFilterType = TypeOp.Number
}

class OperatorMockString(name: String, properties: Map[String, JSerializable])
  extends BaseOperatorMoc(name: String, properties: Map[String, JSerializable]) with ProcessMapAsAny {

  override val castingFilterType = TypeOp.String
}
