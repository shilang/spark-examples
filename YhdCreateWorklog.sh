#!/usr/bin/env bash

today=`date '+%Y-%m-%d'`

spark-submit --class com.yhd.sql.YhdCreateWorklog /root/opensource/spark-examples/target/spark-examples-0.1.0-SNAPSHOT-jar-with-dependencies.jar "wangyuming" "http://trident.yihaodian.com.cn/rest/greenhopper/1.0/xboard/work/allData.json?rapidViewId=357&etag=357%2C1474372263000%2C%2C%2C175&activeQuickFilters=2442&_=1474372710244" "Hm_lvt_ce7370daf17a054325d348babea3ccd6=1474335301; atlassian.xsrf.token=BJOO-3RWZ-LXVM-C1H0|5593a547967feee6dca33785900857ca90f8b54a|lin; JSESSIONID=B956EB9A4649EC113B9CBD7FF02C77AA; Hm_lpvt_ce7370daf17a054325d348babea3ccd6=1474365898; seraph.rememberme.cookie=98167%3Acd8689b323e7afcbc2d165ee94f3194f81df190b" "${today}" 8.0

# crontab commond
# 30 18 * * * source /etc/profile && sh /root/opensource/spark-examples/YhdCreateWorklog.sh 2>&1