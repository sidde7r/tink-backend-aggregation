load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "redsys",
    srcs = glob(["*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/libraries/account_identifier",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_mockito_mockito_core",
    ],
)
