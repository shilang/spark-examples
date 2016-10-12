#!/usr/bin/env bash

today=`date '+%Y-%m-%d'`

spark-submit --class com.yhd.sql.YhdCreateWorklog /tmp/spark-examples-0.1.0-SNAPSHOT-jar-with-dependencies.jar "wangyuming" "Hm_lvt_ce7370daf17a054325d348babea3ccd6=1474335301; atlassian.xsrf.token=BJOO-3RWZ-LXVM-C1H0|2ceaae3ca86ab58c5ac42e5777704d0a1b8fd8e0|lin; JSESSIONID=4CEB987C952B110CC301E2C74D941D2A; Hm_lpvt_ce7370daf17a054325d348babea3ccd6=1476266105" "${today}" 8.0

# crontab commond
# 30 18 * * * source /etc/profile && sh /root/opensource/spark-examples/YhdCreateWorklog.sh 2>&1