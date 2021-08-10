#!/usr/bin/env python

from __future__ import print_function
import os

def pr_ids_from_commit_message(msg):
    """
    >>> pr_ids_from_commit_message("Merge #10 #2002")
    '10,2002'
    >>> pr_ids_from_commit_message("Merge #10 #2002\\n\\n10: Some msg\\n\\n2002: another msg")
    '10,2002'
    """
    firstline = msg.partition('\n')[0]
    return ",".join([s for s in [s.lstrip("#").strip() for s in firstline.split(" ")] if s.isdigit()])

RELEASE_TRAIN_CHARTS = [
    "tink-backend-aggregation",
    "tink-backend-aggregation-java11",
    "tink-backend-aggregation-agents",
    # "tink-backend-integration",
    "tink-backend-aggregation-connectivity-cronjob",
    "tink-backend-aggregation-statuspage-providers-cronjob",
]

CHART_STEP = """
- name: ':kubernetes: Upload Charts'
  branches: "master"
  key: "upload-charts"
  command:
  - echo $$GOOGLE_CLOUD_ACCOUNT_JSON | base64 --decode > /root/credentials.json
  - GOOGLE_APPLICATION_CREDENTIALS=/root/credentials.json .buildkite/upload-charts.sh {charts}
  concurrency: 1
  concurrency_group: "upload-helm-gcs"
  plugins:
  - docker#v3.3.0:
      image: "gcr.io/tink-containers/kubernetes-generator:latest"
      always-pull: True
      environment:
      - "GOOGLE_CLOUD_ACCOUNT_JSON"
      - "VERSION={version}"
"""

TRAIN_STEP = """
- name: "Trigger release-train for {chart}"
  trigger: "release-train"
  branches: "master"
  depends_on:
  - "upload-charts"
  async: true
  build:
    message: "{message}"
    commit: "HEAD"
    branch: "{chart}"
    env:
        CHART: "{chart}"
        REPO_NAME: "tink-backend-aggregation"
        REPO_SHA1: "{repo_sha1}"
        VERSION: "{version}"
        EXPERIMENTAL_CHART_CONTROL_ENABLED: "true"
        PULL_REQUESTS: "{pull_request_ids}"
        CHART_REPO: "tink-charts/"
        CHART_VERSION: "{version}"
"""

SONAR_STEP = """
- name: ":sonarqube: Sonarqube"
  branches: "master"
  command: .buildkite/run-sonar.sh
  timeout_in_minutes: 30
  agents:
    queue: default
  plugins:
    docker-compose#v3.0.3:
      run: "app"
      config: "docker/docker-compose.bazel.yml"
  retry: {"automatic": [{"exit_status": -1, "limit": 2}, {"exit_status": 255, "limit": 2}]}
"""

version = os.environ['VERSION']

print(CHART_STEP.format(
    charts=str(" ").join(RELEASE_TRAIN_CHARTS),
    version=version,
    ))

for chart in RELEASE_TRAIN_CHARTS:
    print(TRAIN_STEP.format(
        chart=chart,
        repo_sha1=os.environ["BUILDKITE_COMMIT"],
        version=version,
        message=os.environ["BUILDKITE_MESSAGE"].splitlines()[0],
        pull_request_ids=pr_ids_from_commit_message(os.environ["BUILDKITE_MESSAGE"]),
    ))

print(SONAR_STEP)
