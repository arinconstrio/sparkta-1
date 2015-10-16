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

import java.sql.Timestamp
import java.util.Date

import com.github.nscala_time.time.Imports._
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import java.io.{Serializable => JSerializable}

@RunWith(classOf[JUnitRunner])
class DateOperationsSpec extends FlatSpec with ShouldMatchers {

  trait CommonValues {

    val granularity = "day"
    val datePattern = "yyyy/MM/dd"
    val expectedPath = "/" + DateTimeFormat.forPattern(datePattern) +
      DateOperations.dateFromGranularity(DateTime.now, granularity)
    val dt = DateTime.now
    val date = new Date()
    val timestamp = new Timestamp(dt.getMillis)
    val minuteDT = dt.withMillisOfSecond(0).withSecondOfMinute(0)
    val hourDT = minuteDT.withMinuteOfHour(0)
    val dayDT = hourDT.withHourOfDay(0)
    val monthDT = dayDT.withDayOfMonth(1)
    val yearDT = monthDT.withMonthOfYear(1)
    val s15DT = DateOperations.roundDateTime(dt, Duration.standardSeconds(15))
    val s10DT = DateOperations.roundDateTime(dt, Duration.standardSeconds(10))
    val s5DT = DateOperations.roundDateTime(dt, Duration.standardSeconds(5))
    val wrongDT = 0L
    val expectedRawPath = "/year=1984/month=03/day=17/hour=13/minute=13/second=13"
  }

  trait FailValues {

    val emptyGranularity = ""
    val badGranularity = "minutely"
    val granularity = "minute"
    val datePattern = Some("yyyy/MM/dd")
    val emptyPattern = None
    val expectedPath = "/0"
    val dt = DateTime.now
    val expectedGranularityPath = "/" + dt.withMillisOfSecond(0).withSecondOfMinute(0).getMillis
    val expectedGranularityWithPattern = "/" + DateTimeFormat.forPattern(datePattern.get).print(dt) + "/" +
      dt.withMillisOfSecond(0).withSecondOfMinute(0).getMillis
  }

  trait ParquetPath {

    val yearStr = "year"
    val monthStr = "month"
    val dayStr = "day"
    val hourStr = "hour"
    val minuteStr = "minute"
    val defaultStr = "whatever"

    val yearPattern = "/'year='yyyy/'"
    val monthPattern = "/'year='yyyy/'month='MM/'"
    val dayPattern = "/'year='yyyy/'month='MM/'day='dd/'"
    val hourPattern = "/'year='yyyy/'month='MM/'day='dd/'hour='HH/'"
    val minutePattern = "/'year='yyyy/'month='MM/'day='dd/'hour='HH/'minute='mm/'"
    val defaultPattern = "/'year='yyyy/'month='MM/'day='dd/'hour='HH/'minute='mm/'second='ss"

    val yearPatternResult = DateTimeFormat.forPattern(yearPattern).print(DateTime.now())
    val monthPatternResult = DateTimeFormat.forPattern(monthPattern).print(DateTime.now())
    val dayPatternResult = DateTimeFormat.forPattern(dayPattern).print(DateTime.now())
    val hourPatternResult = DateTimeFormat.forPattern(hourPattern).print(DateTime.now())
    val minutePatternResult = DateTimeFormat.forPattern(minutePattern).print(DateTime.now())
    val defaultPatternResult = DateTimeFormat.forPattern(defaultPattern).print(DateTime.now())

  }

  "DateOperationsSpec" should "return timestamp with correct parameters" in new CommonValues {
    DateOperations.getTimeFromGranularity(Some(""), Some("s5")) should be(s5DT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("s10")) should be(s10DT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("s15")) should be(s15DT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("minute")) should be(minuteDT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("hour")) should be(hourDT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("day")) should be(dayDT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("month")) should be(monthDT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("year")) should be(yearDT.getMillis)
    DateOperations.getTimeFromGranularity(Some("asdasd"), Some("year")) should be(yearDT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("bad")) should be(wrongDT)
  }

  it should "return parsed timestamp with granularity" in new CommonValues {
    DateOperations.dateFromGranularity(dt, "s5") should be(s5DT.getMillis)
    DateOperations.dateFromGranularity(dt, "s15") should be(s15DT.getMillis)
    DateOperations.dateFromGranularity(dt, "s10") should be(s10DT.getMillis)
    DateOperations.dateFromGranularity(dt, "hour") should be(hourDT.getMillis)
    DateOperations.dateFromGranularity(dt, "day") should be(dayDT.getMillis)
    DateOperations.dateFromGranularity(dt, "month") should be(monthDT.getMillis)
    DateOperations.dateFromGranularity(dt, "year") should be(yearDT.getMillis)
    DateOperations.dateFromGranularity(dt, "bad") should be(wrongDT)
  }

  it should "format path ignoring pattern" in new FailValues {
    DateOperations.subPath(badGranularity, datePattern) should be(expectedPath)
    DateOperations.subPath(badGranularity, datePattern) should be(expectedPath)
    DateOperations.subPath(badGranularity, emptyPattern) should be(expectedPath)
    DateOperations.subPath(granularity, emptyPattern) should be(expectedGranularityPath)
    DateOperations.subPath(granularity, datePattern) should be(expectedGranularityWithPattern)
  }

  it should "create a raw data path" in new CommonValues {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val now = Option(formatter.parseDateTime("1984-03-17 13:13:13"))
    DateOperations.generateParquetPath(now) should be(expectedRawPath)
  }
  it should "round to 15 seconds" in new CommonValues {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val now = formatter.parseDateTime("1984-03-17 13:13:17")
    DateOperations.dateFromGranularity(now, "s15") should be(448373595000L)
  }
  it should "round to 10 seconds" in new CommonValues {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val now = formatter.parseDateTime("1984-03-17 13:13:17")
    DateOperations.dateFromGranularity(now, "s10") should be(448373600000L)
  }
  it should "round to 5 seconds" in new CommonValues {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val now = formatter.parseDateTime("1984-03-17 13:13:17")
    DateOperations.dateFromGranularity(now, "s5") should be(448373595000L)
  }
  it should "create the full parquet path with just a word" in new ParquetPath {
    DateOperations.generateParquetPath(parquetPattern = Some(yearStr)) should be(yearPatternResult)
    DateOperations.generateParquetPath(parquetPattern = Some(monthStr)) should be(monthPatternResult)
    DateOperations.generateParquetPath(parquetPattern = Some(dayStr)) should be(dayPatternResult)
    DateOperations.generateParquetPath(parquetPattern = Some(hourStr)) should be(hourPatternResult)
    DateOperations.generateParquetPath(parquetPattern = Some(minuteStr)) should be(minutePatternResult)
    DateOperations.generateParquetPath(parquetPattern = Some(defaultStr)) should be(defaultPatternResult)

  }

  it should "return millis from a Serializable date" in new CommonValues {
    DateOperations.getMillisFromSerializable(dt.asInstanceOf[JSerializable]) should be(dt.getMillis)
    DateOperations.getMillisFromSerializable(timestamp.asInstanceOf[JSerializable]) should be(dt.getMillis)
    DateOperations.getMillisFromSerializable(date.asInstanceOf[JSerializable]) should be(dt.getMillis)
    DateOperations.getMillisFromSerializable(1L.asInstanceOf[JSerializable]) should be(1L)
    DateOperations.getMillisFromSerializable("1".asInstanceOf[JSerializable]) should be(1L)
  }

  it should "return millis from a Serializable dateTime" in new CommonValues {
    DateOperations.getMillisFromDateTime(dt.asInstanceOf[JSerializable]) should be(dt.getMillis)
    DateOperations.getMillisFromDateTime(timestamp.asInstanceOf[JSerializable]) should be(dt.getMillis)
    DateOperations.getMillisFromDateTime(date.asInstanceOf[JSerializable]) should be(dt.getMillis)
  }

}
