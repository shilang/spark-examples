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

package com.yhd.spark.ml

import java.util.Properties
import com.yhd.spark.common.SparkEntry
import org.apache.spark.sql.SaveMode

/**
 * VM options: -Xms4G -Xmx4G -Xmn768m -XX:PermSize=384m -XX:MaxPermSize=384m
 */
object ReadAndWriteMysql extends SparkEntry{

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("user", "root")
    props.put("password", "root123")
    props.put("driver", "com.mysql.jdbc.Driver")
    val url = "jdbc:mysql://192.168.7.190:3306/cdh?characterEncoding=UTF-8"

    val mysqlDf = spark.read.jdbc(url, "AUDITS", props).cache()
    mysqlDf.show()
    mysqlDf.write.mode(SaveMode.Overwrite).jdbc(url, "testwrite", props)

  }

}
