#!/bin/sh
set -u

GOOD_COMMITS="
fabcc026efb158e25a99cfce63db23c159e47a84
fe552b21f7ce1ef99cb9f3745c512c56487cf12c
673b5f241c2745bcb80b0f1c80d8537e2bb1e77e
e69019746024ae95d8e4bcc0f422ea8c9fa91822
63288c4c3ef36162185c2f7c5aa18b9a4c9f9dc5
b58b4df527f5080aeab6f661ac2f5e64d3de63c0
"

# Commits we already have in our git history that should fail.
BAD_COMMITS="
85480efcbc66b651a7b2a60d5f062e80fc82aad4
784dfd005674cfbaf557bb4a6a3a8540fb0a203b
00e283d84f2d1cdbd3a8aa5970630b171173f198
cd3f6fcb62230de33656a36408c0fad75b6deb88
997d8427a32a34c0fc2cd83006d588e4500ca361
5890c60a6c7f2ed53ba74d49a61bbe093cab9265
035052c9158ece64488656fc996585891188015e
f84e2e432d99479f6c142f7100ebc290dd448488
9f36ab0bad2beb06ebde6f9311e7a8dac82914be
c7d27204cecdbaa049ca74fd0cc4a9a1b0cd63c2
"

FAILED=0

for c in $GOOD_COMMITS;do
  if ! .buildkite/check-commit-metadata.sh "$c" > /dev/null;then
    # Expected the commit to pass. Rerunning again without piping to /dev/null
    # to get the output.
    .buildkite/check-commit-metadata.sh "$c"
    FAILED=1
  fi
done

set +e
for c in $BAD_COMMITS;do
  if .buildkite/check-commit-metadata.sh "$c" > /dev/null;then
    echo "!!! Expected the commit $c to fail. It didn't. !!!"
    FAILED=1
  fi
done

exit $FAILED
