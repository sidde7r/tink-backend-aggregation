#!/bin/bash

# Sync settings with pre process categorization model script

java -classpath src/analytics-jobs/target/analytics-jobs.jar:src/analytics-jobs/target/lib/* se.tink.analytics.jobs.categorization.commands.TrainCategorizationModelCommand --market NL --categoryTypes EXPENSES,INCOME --unhandledCategory expenses:misc.uncategorized
