load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bankdata",
    srcs = glob(["**/*.java"]),
    data = ["//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bankdata/resources"],
    tags = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bankdata",
        "//src/integration/amount",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/refresh",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/account",
        "//src/libraries/serialization",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@tink_backend//src/libraries/account_identifier",
    ],
)

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-fluffy-maintainer",
        "@tink-ab/aggregation-thundercats-maintainer",
        "@tink-ab/aggregation-minion-maintainer",
        "@tink-ab/aggregation-nazguls-maintainer",
        "@tink-ab/aggregation-teletubbies-maintainer",
        "@tink-ab/aggregation-llamas-maintainer",
        "@tink-ab/aggregation-penguins-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
