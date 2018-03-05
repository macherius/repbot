#!/bin/bash
# Detect a running repbot process and kill it.
REPBOT_FIBS_USER=RepBotNG

REPBOT_PID=$(ps ax | grep net.sf.repbot.RepBot | grep -v grep | grep $REPBOT_FIBS_USER | cut -c1-5 | paste -s -)
echo Killing service for fibs user $REPBOT_FIBS_USER with PID $REPBOT_PID
kill $REPBOT_PID

