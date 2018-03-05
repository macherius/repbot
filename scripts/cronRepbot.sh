#!/bin/bash

REPBOT_HOME=/home/repbot
REPBOT_FIBS_USER=RepBotNG
REPBOT_RUN=$REPBOT_HOME/runRepbot.sh

REPBOT_PID=$(ps ax | grep net.sf.repbot.RepBot | grep -v grep | grep $REPBOT_FIBS_USER | cut -c1-5 | paste -s -)

if [[ $REPBOT_PID ]]; then
        echo Running with PID $REPBOT_PID for fibs user $REPBOT_FIBS_USER
else
        echo Not running for fibs user $REPBOT_FIBS_USER
        cd $REPBOT_HOME
        $REPBOT_RUN 1>&2 2>/dev/null </dev/null &
        exit 1
fi

