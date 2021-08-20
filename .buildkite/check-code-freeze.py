#!/usr/bin/env python3

import os, sys

commit_message = os.getenv('BUILDKITE_MESSAGE')
if os.getenv("BUILDKITE_BRANCH") == 'staging':
    merge_info_line = commit_message.split("\n")[0]
    if merge_info_line.count('#') != commit_message.count('CODEFREEZE_OVERRIDE'):
        os.system('echo "A code freeze is in effect. Please see #reliability-taskforce for more information" | buildkite-agent annotate')
        sys.exit(1)
