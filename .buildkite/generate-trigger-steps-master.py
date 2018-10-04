#!/usr/bin/env python

from __future__ import print_function
import os

BRANCHES = {
    'aggregation-production': {'block': True},
    'aggregation-staging': {'block': True},
}

PROJECTS = {
    'tink-backend-aggregation': {
        'chart': False,
        'salt': True,
        'branches': [
            'aggregation-production',
            'aggregation-staging'
        ],},
    'tink-backend-provider-configuration': {
        'chart': True,
        'salt': False,
        'branches': [
            'aggregation-production',
            'aggregation-staging',
        ],
    },
}

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

version = os.environ['VERSION']

for project, project_settings in PROJECTS.items():
    for branch in project_settings['branches']:
        settings = BRANCHES[branch]
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
