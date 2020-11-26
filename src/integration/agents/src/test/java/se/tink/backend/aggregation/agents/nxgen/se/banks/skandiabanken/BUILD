load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "skandiabanken",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:agent_unit_tests",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/skandiabanken",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/libraries/serialization",
        "//third_party:org_mockito_mockito_core",
    ],
)
