#!/usr/bin/env bash

today=`date '+%Y-%m-%d'`

spark-submit --class com.yhd.sql.YhdCreateWorklog /tmp/spark-examples-0.1.0-SNAPSHOT-jar-with-dependencies.jar "wangyuming" "Hm_lvt_ce7370daf17a054325d348babea3ccd6=1478079441; atlassian.xsrf.token=BJOO-3RWZ-LXVM-C1H0|ecedf338181273bb4050535ebc46c99dee397c00|lin; JSESSIONID=D2D306EDFCECA2C70F49C87149B43003; Hm_lpvt_ce7370daf17a054325d348babea3ccd6=1478166275; seraph.rememberme.cookie=101448%3A3a1373e881635f0384ac52e30917ea34859a7326" "${today}" 8.0

# crontab commond
# 30 18 * * * source /etc/profile && sh /root/opensource/spark-examples/YhdCreateWorklog.sh 2>&1
