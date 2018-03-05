#!/bin/bash

source $HOME/.bash_aliases

JARHOME=$HOME/.m2/repository
PGJDBCJAR=$JARHOME/postgresql/postgresql/9.2-1002.jdbc4/postgresql-9.2-1002.jdbc4.jar
MYSQLJDBCJAR=$JARHOME/mysql/mysql-connector-java/5.1.23/mysql-connector-java-5.1.23.jar
REPBOTJAR=$JARHOME/org/openfibs/Repbot/2.2.0/Repbot-2.2.0.jar
 
java \
-Dmysqlurl=$MYSQL_REPBOT_DB_URL \
-Dmysqluser=$MYSQL_REPBOT_DB_USER \
-Dmysqlpassword=$MYSQL_REPBOT_DB_PASSWORD \
-Dpgurl=$POSTGRESQL_REPBOT_DB_URL \
-Dpguser=$POSTGRESQL_REPBOT_DB_USER \
-Dpgpassword=$POSTGRESQL_REPBOT_DB_PASSWORD \
-classpath $REPBOTJAR:$MYSQLJDBCJAR:$PGJDBCJAR \
net.sf.repbot.db.copy.CopyDB