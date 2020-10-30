Here's a directory containing useful git hooks if you're into that. To install:

```sh
$ git config core.hooksPath .githooks
```

```sh
$ cd .git/hooks
$ ln -s ../../.githooks/post-commit
$ ln -s ../../.githooks/pre-push
```

To skip the hooks `commit-msg` and `pre-commit` when performing a commit,
use `git commit --no-verify` or `git commit -n`.

To skip the hook `pre-push` when performing a push,
use `git push --no-verify`.
