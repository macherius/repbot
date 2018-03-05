#!/bin/sh
mysqldump  \
        --no-create-info \
        --databases repbot \
        --user=repbot \
        --skip-triggers \
        --compact \
        --tables news users opinions \
        --user=repbot \
        --password

