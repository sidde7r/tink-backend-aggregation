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
    "tink-backend-aggregation-agents",
    # "tink-backend-integration",
    "tink-backend-aggregation-connectivity-cronjob",
    "tink-backend-aggregation-statuspage-providers-cronjob",
]

TRAIN_STEP = """
- name: "Trigger release-train for {chart}"
  trigger: "release-train"
  branches: "master"
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
"""

version = os.environ['VERSION']

for chart in RELEASE_TRAIN_CHARTS:
    print(TRAIN_STEP.format(
        chart=chart,
        repo_sha1=os.environ["BUILDKITE_COMMIT"],
        version=version,
        message=os.environ["BUILDKITE_MESSAGE"].splitlines()[0],
        pull_request_ids=pr_ids_from_commit_message(os.environ["BUILDKITE_MESSAGE"]),
    ))
