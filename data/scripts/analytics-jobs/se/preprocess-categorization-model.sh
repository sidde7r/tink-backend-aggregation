#!/bin/bash

# Sync settings with train categorization model script

tink-spark-submit.py \
--log-level DEBUG \
--verbose-driver \
--jar  /usr/share/tink-backend-analytics/analytics-jobs.jar \
--config-file /etc/tink/tink-analytics.properties \
--lib /usr/share/tink-backend-analytics/lib \
se.tink.analytics.jobs.categorization.jobs.PrepareCategorizationModelJob \
"--source database --market SE --categoryTypes EXPENSES,INCOME --unhandledCategory expenses:misc.uncategorized --startDate 2016-01-01"
