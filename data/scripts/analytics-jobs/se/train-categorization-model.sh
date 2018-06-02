#!/bin/bash

# Sync settings with pre process categorization model script

java -verbose -classpath /usr/share/tink-backend-analytics/analytics-jobs.jar:/usr/share/tink-backend-analytics/lib/* se.tink.analytics.jobs.categorization.commands.TrainCategorizationModelCommand --market SE --categoryTypes EXPENSES,INCOME --unhandledCategory expenses:misc.uncategorized
