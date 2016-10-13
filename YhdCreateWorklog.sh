#!/usr/bin/env bash

today=`date '+%Y-%m-%d'`

spark-submit --class com.yhd.sql.YhdCreateWorklog /tmp/spark-examples-0.1.0-SNAPSHOT-jar-with-dependencies.jar "wangyuming" "Hm_lvt_ce7370daf17a054325d348babea3ccd6=1474852250; JSESSIONID=D71CFF9F1A4C63B91839DE01E58FE399; atlassian.xsrf.token=BJOO-3RWZ-LXVM-C1H0|565fe26f3fc29310834a4b4957c589d020c73d71|lin; Hm_lpvt_ce7370daf17a054325d348babea3ccd6=1476342794; seraph.rememberme.cookie=98954"%"3A3d13d25dc43ab6f9b0510764c4a49e4b4daa25be" "${today}" 8.0

# crontab commond
# 30 18 * * * source /etc/profile && sh /root/opensource/spark-examples/YhdCreateWorklog.sh 2>&1
