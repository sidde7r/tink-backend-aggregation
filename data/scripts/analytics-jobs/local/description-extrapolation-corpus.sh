#!/bin/bash

data/scripts/tink-spark-submit.py \
--no-dse \
--log-level INFO \
--verbose-driver \
--jar  src/analytics-jobs/target/analytics-jobs.jar \
--config-file etc/development-tink-analytics.properties \
--lib src/analytics-jobs/target/lib \
se.tink.analytics.jobs.SeedDescriptionExtrapolationCorpusJob "--source database"
