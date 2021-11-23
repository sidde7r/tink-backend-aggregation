load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "citadele",
    srcs = glob(["**/*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/citadele",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/agents/componentproviders",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filterable/request",
        "//src/libraries/agent-storage",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
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
