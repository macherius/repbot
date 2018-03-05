#!/bin/bash

# sudo -s -H -u postgres

echo "** Dropping DB user repbot"
echo "Enter DB User postgres password"
dropuser \
--username=postgres \
--password \
--host=localhost \
--if-exists \
repbot

echo "** Creating DB user repbot"
echo "Enter new DB User repbot password twice, then DB User postgres password"
createuser \
--username=postgres \
--password \
--host=localhost \
--login \
--inherit \
--no-superuser \
--createdb \
--no-createrole \
--pwprompt \
--encrypted \
repbot

echo "** Dropping DB repbot"
echo "Enter DB User repbot password"
dropdb \
--username=repbot \
--password \
--host=localhost \
--if-exists \
repbot

echo "** Creating DB repbot"
echo "Enter DB User repbot password"
createdb \
--username=repbot \
--password \
--host=localhost \
--encoding=UTF8 \
--owner=repbot \
repbot
