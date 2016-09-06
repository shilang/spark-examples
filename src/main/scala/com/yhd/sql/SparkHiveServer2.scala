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

import java.sql.DriverManager

/**
 *
 */
object SparkHiveServer2 {
  def main(args: Array[String]) {

    Class.forName("org.apache.hive.jdbc.HiveDriver")
    val conn = DriverManager.getConnection("jdbc:hive2://10.17.221.20:20402/default", "pms", "")
    val stmt = conn.createStatement()
    val sql = "select * from tmp.wym_01 limit 10"
    val res = stmt.executeQuery(sql)
    while (res.next()) {
      for (i <- 1 to 5) {
        // scalastyle:off
        println("Value of i: " + res.getString(i))
        // scakastyle:on
      }
    }
    conn.close()
  }
}