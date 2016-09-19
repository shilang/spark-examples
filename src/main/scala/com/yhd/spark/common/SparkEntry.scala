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

package com.yhd.spark.common

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * Spark application entry
 */
class SparkEntry extends Logging{

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  val conf = new SparkConf()
  if (Runtime.getRuntime.availableProcessors() < 10) {
    conf.setMaster("local[*]")
  }
  logInfo("Debug string: " + conf.toDebugString)
  val spark = SparkSession.builder.config(conf).enableHiveSupport().getOrCreate()
  val sc = spark.sparkContext
  import spark.implicits._
}
