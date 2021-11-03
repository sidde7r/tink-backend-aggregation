load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "api",
    srcs = glob(["**/*.java"]),
    data = ["//:agent_test_data"],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/legacy/creditcards/ikano/api",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/models",
        "//src/libraries/date",
        "//src/libraries/social_security_number/src/test/java/se/tink/libraries/social/security/time",
        "//third_party:org_assertj_assertj_core",
    ],
)
