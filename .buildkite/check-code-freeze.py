#!/usr/bin/env python3

import os, sys

commit_message = os.getenv('BUILDKITE_MESSAGE')
if os.getenv("BUILDKITE_BRANCH") == 'staging':
    if commit_message.count('Merge #') != commit_message.count('CODEFREEZE_OVERRIDE'):
        os.system('echo "A code freeze is in effect. Please see #reliability-taskforce for more information" | buildkite-agent annotate')
        sys.exit(1)
