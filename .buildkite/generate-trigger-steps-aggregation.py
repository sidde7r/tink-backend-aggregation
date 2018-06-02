#!/usr/bin/env python

from __future__ import print_function
import os

BRANCHES = {
    'oxford-production': {'block': False},
    'oxford-staging': {'block': False},
    'leeds-production': {'block': False},
    'leeds-staging': {'block': False},
    'cornwall-production': {'block': False},
    'cornwall-testing': {'block': False},
    'kirkby-production': {'block': False},
    'kirkby-staging': {'block': False},
    'newport-production': {'block': False},
    'newport-staging': {'block': False},
    'farnham-staging': {'block': False},
    'aggregation-production': {'block': False},
}

PROJECTS = {
    'tink-backend-aggregation': {'chart': False, 'salt': True},
}

STEP = """
- name: "Trigger release {branch} {project}"
  trigger: "{pipeline}"
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
      TINK_CHART_REPO: "tink-backend"
      TINK_SALT_DEPLOY: "{salt_deploy}"
      TINK_KUBERNETES_DEPLOY: "{kubernetes_deploy}"
"""

BLOCK_STEP = """
- block: Release & deploy aggregation {version} to all clusters
"""

version = os.environ['TINK_VERSION']

print(BLOCK_STEP.format(version=version))

for project, project_settings in PROJECTS.items():
    for branch, settings in BRANCHES.items():
        if settings.get('block'):
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
