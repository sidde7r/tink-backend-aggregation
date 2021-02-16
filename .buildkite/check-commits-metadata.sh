#!/bin/bash
set -u

# Make sure commit messages we expect should fail are actually failing. This is
# somewhat a hack to avoid having yet another build step for such a silly
# thing.
set -e
echo "Running self-test:"
.buildkite/test-check-commit-metadata.sh
echo "Done running self-test."
set +e

# Ignore commits that where imported from tink-oauth
# git-log prints range exclusive the start/end
IGNORED_COMMITS=$(cat <<EOF
e42fd5083ea
$(git log --format=format:%H e42fd5083ea...90d8d627836)
90d8d627836
EOF
);

# Run the actual tests.
FAILED=0
for commit in $(git log --format=format:%H origin/master..HEAD);do
  
  if [[ "$IGNORED_COMMITS" = *$commit* ]]; then
    echo "Ignoring commit ${commit} imported from tink-oauth";
    continue;
  fi

  if ! .buildkite/check-commit-metadata.sh "$commit";then
    FAILED=1
  fi
done

if [ $FAILED -eq 1 ];then
  exit 1
fi
