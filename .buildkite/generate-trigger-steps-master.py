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

PROJECTS = {
    'tink-backend-aggregation-agents': {
        'chart': True,
        'salt': False,
        'branches': {
            'aggregation-production': {'block': False},
            'aggregation-staging': {'block': False},
        },
    },
    'tink-backend-aggregation-statuspage-providers-cronjob': {
        'chart': True,
        'salt': False,
        'branches': {
            'aggregation-production': {'block': True},
        },
    }
}

RELEASE_TRAIN_CHARTS = [
    "tink-backend-aggregation",
    "tink-backend-integration",
    "tink-backend-notifying-aggregation-service-asserter",
    "tink-backend-aggregation-connectivity-cronjob",
]

STEP = """
- name: "Trigger release {branch} {project}"
  trigger: "{pipeline}"
  branches: master
  async: true
  build:
    message: "Release {project} {version} to {branch}"
    commit: "HEAD"
    branch: "{branch}"
    env:
      TINK_PROJECT: "{project}"
      TINK_VERSION: "{version}"
      TINK_BRANCH: "{branch}"
      TINK_BLOCK: "{block}"
      TINK_CHART_REPO: "tink-backend-aggregation"
      TINK_SALT_DEPLOY: "{salt_deploy}"
      TINK_KUBERNETES_DEPLOY: "{kubernetes_deploy}"
"""

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

for project, project_settings in PROJECTS.items():
    for branch, branch_settings in project_settings['branches'].items():
        if branch_settings.get('block', True):
            block = 'true'
        else:
            block = ''

        kubernetes_deploy = project_settings.get('chart', False)
        salt_deploy = project_settings.get('salt', False)

        print(STEP.format(
            block=block,
            branch=branch,
            kubernetes_deploy=kubernetes_deploy,
            pipeline='release-{}'.format(project),
            project=project,
            salt_deploy=salt_deploy,
            version=version,
        ))
