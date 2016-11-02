#!/usr/bin/env bash

today=`date '+%Y-%m-%d'`

spark-submit --class com.yhd.sql.YhdCreateWorklog /tmp/spark-examples-0.1.0-SNAPSHOT-jar-with-dependencies.jar "wangyuming" "Hm_lvt_ce7370daf17a054325d348babea3ccd6=1478079441; atlassian.xsrf.token=BJOO-3RWZ-LXVM-C1H0|c14323e26b45079a9bb1d9cb93c6e85c188005a9|lin; JSESSIONID=F114ED1C1DD60139592E020D739DCCEE; Hm_lpvt_ce7370daf17a054325d348babea3ccd6=1478079610" "${today}" 8.0

# crontab commond
# 30 18 * * * source /etc/profile && sh /root/opensource/spark-examples/YhdCreateWorklog.sh 2>&1
