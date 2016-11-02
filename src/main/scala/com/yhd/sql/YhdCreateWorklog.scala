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
import org.jsoup.Jsoup

import scala.sys.process._
import scala.util.Random

/**
 *
 */
object YhdCreateWorklog extends SparkEntry {
  def main(args: Array[String])  {

    if (args.length < 4) {
      logError("Required at least 4 args: userName, cookie, date, hours or TridentID")
      System.exit(-1)
    }

    val userName: String = args(0)
    val cookie = args(1)
    val date = args(2)
    val hours = args(3)
    val tridentID: String = if (args.length == 5) args(4) else ""

    if (!isWeekday(date)) {
      logWarning(date + " is not weekday.")
      System.exit(0)
    }

    val workList = Seq("curl",
      "http://trident.yihaodian.com.cn/rest/issueNav/1/issueTable/?jql=issuetype+in+" +
        "subTaskIssueTypes()+AND+resolution+%3D+Unresolved+AND+status+%3D+%22In+Progress%22+AND+" +
        "assignee+in+(" + userName + ")+ORDER+BY+updatedDate+DESC" +
        "&useUserColumns=true&filterId=-1&_=1474624648848",
      "-H", "Host: trident.yihaodian.com.cn",
      "-H", "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:45.0) Gecko/20100101 Firefox/45.0",
      "-H", "Accept: */*",
      "-H", "Accept-Language: en-US,en;q=0.8,zh-CN;q=0.5,zh;q=0.3",
      "-H", "X-AUSERNAME: " + userName,
      "-H", "X-Requested-With: XMLHttpRequest",
      "-H", "Referer: http://trident.yihaodian.com.cn",
      "-H", "Cookie: " + cookie,
      "-H", "Connection: keep-alive"
    )

    val workListHtml = workList !!

    logInfo(workListHtml)

    val issueRows = Jsoup.parse(workListHtml.replace("\\n", "").replace("\\\"", "\""))
      .getElementsByClass("issuerow")
    if (issueRows.size() == 0) {
      logWarning("Please check your cookie.")
    }
    var issueIds = Map[String, Long]()
    for (i <- 0 until issueRows.size()) {
      val parent = issueRows.get(i).children().parents()
      val tridentId = parent.attr("data-issuekey")
      val issueId = parent.attr("rel").toLong
      issueIds = issueIds + (tridentId -> issueId)
    }

    val taskHours = if (!tridentID.isEmpty && issueIds.get(tridentID).isDefined) {
      getTaskHours(issueIds.get(tridentID).toSeq, hours.toDouble)
    } else {
      getTaskHours(issueIds.values.toSeq, hours.toDouble)
    }

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
      if (l._2.toString.toDouble > 0.0) {
        logWarning(worklog !!)
      }
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
    for (i <- 0 until manHours.numTask.toInt) {
      taskHours.add(0.0)
    }
    val index = Random.nextInt(manHours.numTask.toInt)
    taskHours.set(index.toInt, manHours.hours.doubleValue())
    taskHours
  }

  def isWeekday(dateStr: String): Boolean = {
    var isWeekday = false
    val date = dateStr.split("-").map(_.toInt)
    val calendar = new GregorianCalendar(date(0), date(1) - 1, date(2))
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    logWarning("dayOfWeek: " + dayOfWeek)
    if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
      isWeekday = false
    } else {
      isWeekday = true
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

    // 20/九月/16 06:18 下午
    val tridentDate = day + "/" + month + "/" + year + " 09:00 上午"
    logWarning("tridentDate: " + tridentDate)
    URLEncoder.encode(tridentDate, "UTF-8")
  }
}
