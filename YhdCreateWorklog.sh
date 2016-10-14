#!/usr/bin/env bash

today=`date '+%Y-%m-%d'`

spark-submit --class com.yhd.sql.YhdCreateWorklog /tmp/spark-examples-0.1.0-SNAPSHOT-jar-with-dependencies.jar "wangyuming" "Hm_lvt_ce7370daf17a054325d348babea3ccd6=1474335301; atlassian.xsrf.token=BJOO-3RWZ-LXVM-C1H0|2bac9dba731cf04ac678b7227991f35dc82ec0ec|lin; JSESSIONID=10667EDC28E5F5DE983D8E27E228AB63; Hm_lpvt_ce7370daf17a054325d348babea3ccd6=1476413858; seraph.rememberme.cookie=100026%3A23b136de793e29dc3b82839a54d1d3f730cad60d" "${today}" 8.0

# crontab commond
# 30 18 * * * source /etc/profile && sh /root/opensource/spark-examples/YhdCreateWorklog.sh 2>&1
