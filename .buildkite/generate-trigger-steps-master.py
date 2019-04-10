#!/usr/bin/env python

from __future__ import print_function
import os

PROJECTS = {
    'tink-backend-aggregation': {
        'chart': True,
        'salt': False,
        'branches': {
            'aggregation-production': {'block': True},
            'aggregation-staging': {'block': False},
        },
    },
    'tink-backend-provider-configuration': {
        'chart': True,
        'salt': False,
        'branches': {
            'aggregation-production': {'block': True},
            'aggregation-staging': {'block': False},
        },
    },
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
