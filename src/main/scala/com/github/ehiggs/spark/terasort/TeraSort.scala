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

package com.github.ehiggs.spark.terasort

import java.util.Comparator
import com.google.common.primitives.UnsignedBytes
import org.apache.spark.{SparkConf, SparkContext}
import java.util.Timer
import org.apache.spark.util
import java.lang.System;

/**
 * This is a great example program to stress test Spark's shuffle mechanism.
 *
 * See http://sortbenchmark.org/
 */
object TeraSort {

  implicit val caseInsensitiveOrdering : Comparator[Array[Byte]] =
    UnsignedBytes.lexicographicalComparator

  def main(args: Array[String]) {

    if (args.length < 3) {
      println("Usage:")
      println("DRIVER_MEMORY=[mem] spark-submit " +
        "com.github.ehiggs.spark.terasort.TeraSort " +
        "spark-terasort-1.0-SNAPSHOT-with-dependencies.jar " +
        "[input-file] [output-file]")
      println(" ")
      println("Example:")
      println("DRIVER_MEMORY=50g spark-submit " +
        "com.github.ehiggs.spark.terasort.TeraSort " +
        "spark-terasort-1.0-SNAPSHOT-with-dependencies.jar " +
        "/home/myuser/terasort_in /home/myuser/terasort_out")
      System.exit(0)
    }

    // Process command line arguments
    val inputFile = args(0)
    val outputFile = args(1)
    val timeout = args(2).toInt // mania


    val conf = new SparkConf()
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .setAppName(s"TeraSort")
    val sc = new SparkContext(conf)

    // mania
    val t = new java.util.Timer()
    val task = new java.util.TimerTask {
        def run() {
            println("YYYOOOOOOOOOOOOOO")
            //sc.session.stop();

            this.cancel()
            sc.cancelAllJobs()
            sc.stop()
            System.exit(0)
        }
    } 
    t.schedule(task, timeout*1000, timeout*1000)

    while(true) { // mania
    val dataset = sc.newAPIHadoopFile[Array[Byte], Array[Byte], TeraInputFormat](inputFile)
    val sorted = dataset.repartitionAndSortWithinPartitions(
      new TeraSortPartitioner(dataset.partitions.length))
    sorted.saveAsNewAPIHadoopFile[TeraOutputFormat](outputFile)
    }
    System.exit(0) //explicitly exiting
  }
}
