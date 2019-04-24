# Connectivity Test Cronjob

To be able to test locally

1. Build docker build -t local_connectivity_test jobs/cron/demo-bank
2. kubectl run -n aggregation tests --image=local_connectivity_test:latest --image-pull-policy=Never