#!/bin/sh

set -eu

COMMIT=$1

EXITCODE=0
print_error_header () {
  if [ $EXITCODE -ne 0 ];then
    return
  else
    EXITCODE=1
  fi
  echo "^^^ +++"
  echo "The commit"
  echo
  git show --pretty=fuller --no-expand-tabs --no-patch "$1" | sed 's/^/    /'
  echo
  echo "did not pass linting. Consider fixing using interactive rebase"
  echo "(https://bit.ly/2KvaLtO) or 'git commit --amend'. Errors:"
  echo
}

echo "--- Checking commit ${COMMIT}..."

# Extract the first line of the commit reference.
#
# This could be done in a single statement, but doing it in two since Alpine's
# shell doesn't support '-o pipefail' setting. Without that flag, 'git' might
# fail without us knowing.
COMMIT_MSG_SUBJECT=$(git show --pretty=format:'%s' --no-expand-tabs --no-patch "$COMMIT")

if [ ${#COMMIT_MSG_SUBJECT} -gt 72 ];then
  print_error_header "$COMMIT"
  echo " * The subject (first line) is too long. Max length should be 72 characters."
  echo "   See CONTRIBUTING.md for details."
fi

# The exact format of commit messages is described in `/CONTRIBUTING.md`.
EXPECTED_COMMIT_MSG_SUBJECT_FORMAT='^(build|chore|ci|copy|docs|feat|fix|log|perf|refactor|revert|style|test)(\([^()]+\))?: .+$'

# ALLOWED_COMMIT_MSG_SUBJECT_FORMAT also includes the commit message that bors generates.
ALLOWED_COMMIT_MSG_SUBJECT_FORMAT="^Merge( #[0-9]+)+$|${EXPECTED_COMMIT_MSG_SUBJECT_FORMAT}"

# Commit message for Try: https://github.com/bors-ng/bors-ng/blob/2121fae8bd0b6e6e3779b5aa563cce4b2f9aea03/lib/worker/attemptor.ex#L206
ALLOWED_COMMIT_MSG_SUBJECT_FORMAT="^Try #[0-9]+:(.*?)$|${ALLOWED_COMMIT_MSG_SUBJECT_FORMAT}"


set +e
echo "$COMMIT_MSG_SUBJECT" | grep --quiet --extended-regexp "$ALLOWED_COMMIT_MSG_SUBJECT_FORMAT"
exitCode=$?
if [ $exitCode -ne 0 ]; then
  print_error_header "$COMMIT"
  echo " * The subject (first line) is not matching the expected regular expression"
  echo
  echo "       $EXPECTED_COMMIT_MSG_SUBJECT_FORMAT"
  echo
  echo "   . See CONTRIBUTING.md for details."
fi
set -e

if git show --pretty=format:'%B' --no-expand-tabs --no-patch "$COMMIT" | awk 'NR==2 { print; }' | grep -Eqv '^\s*$';then
  print_error_header "$COMMIT"
  echo " * The second line in commit message must be an empty string."
fi

check_name () {
  if echo "$1" | grep -qE 'tink-bors-ng\[bot\]|dependabot-preview\[bot\]|GitHub';then
    return
  fi

  set +e
  if echo "$1" | grep -qv " "; then
    print_error_header "$COMMIT"
    echo " * The $2 name is missing a space. Is the full name, including last"
    echo "   name, set? Tink is growing fast and, if it hasn't happened yet,"
    echo "   someone will soon share your first name. See [1] for how to correct"
    echo "   this."
    echo
    echo "   [1] https://help.github.com/en/articles/setting-your-username-in-git#setting-your-git-username-for-every-repository-on-your-computer"
  fi
  set -e
}
COMMIT_AUTHOR_NAME=$(git show --pretty=format:'%an' --no-expand-tabs --no-patch "$COMMIT")
check_name "$COMMIT_AUTHOR_NAME" "author"
COMMIT_COMMITTER_NAME=$(git show --pretty=format:'%cn' --no-expand-tabs --no-patch "$COMMIT")
check_name "$COMMIT_COMMITTER_NAME" "committer"

exit $EXITCODE
