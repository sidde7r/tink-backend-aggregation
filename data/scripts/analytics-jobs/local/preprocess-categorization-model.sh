#!/bin/bash

# Sync settings with train categorization model script

data/scripts/tink-spark-submit.py \
--log-level DEBUG \
--verbose-driver \
--jar  src/analytics-jobs/target/analytics-jobs.jar \
--config-file etc/development-tink-analytics.properties \
--lib src/analytics-jobs/target/lib \
se.tink.analytics.jobs.categorization.jobs.PrepareCategorizationModelJob \
"--source database --market NL --categoryTypes EXPENSES,INCOME --unhandledCategory expenses:misc.uncategorized --startDate 2000-01-01"
