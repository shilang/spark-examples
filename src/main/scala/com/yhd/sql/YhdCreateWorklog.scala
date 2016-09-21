/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yhd.sql

import java.net.URLEncoder
import java.util
import java.util.{Calendar, GregorianCalendar}

import com.yhd.spark.common.SparkEntry
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.map.ObjectMapper

import scala.sys.process._
import scala.util.Random

/**
 *
 */
object YhdCreateWorklog extends SparkEntry {
  def main(args: Array[String])  {

    if (args.size != 5) {
      logError("Required 5 args: userName, workListUrl, cookie, date and hours")
      System.exit(0)
    }

    val userName = args(0)
    val workListUrl = args(1)
    val cookie = args(2)
    val date = args(3)
    val hours = args(4)

    if (!isWeekday(date)) {
      logWarning(date + " is not Weekday.")
      System.exit(0)
    }

    val workList = Seq("curl",
      workListUrl,
      "-X", "GET",
      "-H", "Host: trident.yihaodian.com.cn",
      "-H", "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:45.0) Gecko/20100101 Firefox/45.0",
      "-H", "Accept: application/json, text/javascript, */*; q=0.01",
      "-H", "Accept-Language: en-US,en;q=0.8,zh-CN;q=0.5,zh;q=0.3",
      "-H", "Content-Type: application/json",
      "-H", "X-AUSERNAME: " + userName,
      "-H", "X-Requested-With: XMLHttpRequest",
      "-H", "Referer: http://trident.yihaodian.com.cn",
      "-H", "Cookie: " + cookie,
      "-H", "Connection: keep-alive",
      "-H", "X-Atlassian-GreenHopper-Gadget: false"
    )

    val workListJson = workList !!

    val mapper = new ObjectMapper()
    val root = mapper.readValue[JsonNode](workListJson, classOf[JsonNode])
    logInfo(workListJson)

    val issues = sc.parallelize(Seq(root.get("issuesData").get("issues").toString))

    val sparkIssues = spark.read.json(issues)
    logInfo("issues count: " + sparkIssues.count())
    import spark.implicits._
    val ids = sparkIssues.where($"statusName" === "In Progress").select("id")
    val runningIds = ids.collect().map(_.getLong(0)).mkString(",")

    val taskHours = getTaskHours(ids.collect().map(_.getLong(0)), hours.toDouble)
    taskHours.foreach{ l =>
      logWarning(l._1 + ": " + l._2)
      val worklog = Seq("curl",
        "http://trident.yihaodian.com.cn/secure/YhdCreateWorklog.jspa",
        "-X", "POST",
        "-H", "Host: trident.yihaodian.com.cn",
        "-H", "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:45.0) Gecko/20100101 Firefox/45.0",
        "-H", "Accept: text/html, */*; q=0.01",
        "-H", "Accept-Language: en-US,en;q=0.8,zh-CN;q=0.5,zh;q=0.3",
        "-H", "Content-Type: application/x-www-form-urlencoded",
        "-H", "X-AUSERNAME: " + userName,
        "-H", "X-Requested-With: XMLHttpRequest",
        "-H", "Referer: http://trident.yihaodian.com.cn",
        "-H", "Cookie: " + cookie,
        "-H", "Connection: keep-alive",
        "--data", "inline=true&decorator=dialog&worklogId=&id=" + l._1 + "&timeLogged=" +
          l._2 + "h&startDate=" + convertToTridentDate(date) +
          "&adjustEstimate=auto&comment=&commentLevel=&" +
          "atl_token=BJOO-3RWZ-LXVM-C1H0%7C5593a547967feee6dca33785900857ca90f8b54a%7Clin"
      )
      logWarning(worklog !!)
    }
  }

  class ManHours(var numTask: Long, var hours: BigDecimal) {}

  def getTaskHours(taskIds: Seq[Long], hours: Double): Seq[(Long, AnyRef)] = {
    val manHours = new ManHours(taskIds.size, BigDecimal(hours))
    val taskHours = taskIds.zip(
      getIndividualHours(new util.ArrayList[Double](taskIds.size), manHours).toArray())
    taskHours
  }

  def getIndividualHours(taskHours: util.ArrayList[Double], manHours: ManHours):
  util.ArrayList[Double] = {
   if (manHours.numTask == 0) {
     taskHours
   } else {
     var individualHours : Double = 0.0
     if (manHours.numTask == 1) {
       manHours.numTask = manHours.numTask - 1
       individualHours = manHours.hours.doubleValue()
     } else {
       val random = new Random()
       val min: Double = 0.0
       val max: Double = manHours.hours.doubleValue() / manHours.numTask * 2
       individualHours = random.nextDouble() * max
       individualHours = if (individualHours < min) min else individualHours
       individualHours = Math.floor(individualHours * 10) / 10
       manHours.numTask = manHours.numTask - 1
       manHours.hours = manHours.hours - individualHours
     }
     taskHours.add(individualHours)
     getIndividualHours(taskHours, manHours)
   }
  }

  def isWeekday(dateStr: String): Boolean = {
    var isWeekday = false
    val date = dateStr.split("-").map(_.toInt)
    val calendar = new GregorianCalendar(date(0), date(1), date(2))
    val month = calendar.get(Calendar.MONTH)
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    if (month == 10) {
      if (dayOfMonth <= 7) {
        isWeekday = false
      } else if (dayOfMonth == 8 || dayOfMonth == 9) {
        isWeekday = true
      }
    } else {
      if (dayOfWeek == 2 || dayOfWeek == 3) {
        isWeekday = false
      } else {
        isWeekday = true
      }
    }
    isWeekday
  }

  def convertToTridentDate(dateStr: String): String = {
    val chineseMonth = Seq("", "一月", "二月", "三月", "四月", "五月", "六月", "七月",
      "八月", "九月", "十月", "十一月", "十二月")
    val date = dateStr.split("-").map(_.toInt)
    val year = date(0) - 2000
    val month = chineseMonth(date(1))
    val day = date(2)
    val random = Random

    // 20/九月/16 06:18 下午
    val tridentDate = day + "/" + month + "/" + year + " 06:" + (10 + random.nextInt(49)) + " 下午"
    logWarning("tridentDate: " + tridentDate)
    URLEncoder.encode(tridentDate, "UTF-8")
  }
}
