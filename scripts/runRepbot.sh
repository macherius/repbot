#!/bin/sh

FIBS_USER=RepBotNG
FIBS_PASSWORD=*********

DB_USER=repbot
DB_PASSWORD=*********

JAR_DIR=/home/repbot/lib
# see jdbc.postgresql.org/documentation/head/connect.html
CP=${JAR_DIR}/postgresql-9.2-1002.jdbc4.jar:${JAR_DIR}/Repbot-2.2.0.jar

java -XX:+UseCompressedOops \
        -Djava.awt.headless=true \
        -Dfibshost=fibs.com \
        -Dfibsport=4321 \
        -Dfibsuser=$FIBS_USER \
        -Dfibspassword=$FIBS_PASSWORD \
        -Dnetworkaddress.cache.ttl=60 \
        -Ddburl=jdbc:postgresql://localhost/repbot \
        -Ddbuser=$DB_USER \
        -Ddbpassword=$DB_PASSWORD \
        -Dgreeting="" \
        -Dbot_voucher_weight=0 \
        -Dbot_complaint_weight=0 \
        -Dmax_opinion_weight=10000 \
        -Dreap_delay=400 \
        -classpath $CP \
        net.sf.repbot.RepBot 

