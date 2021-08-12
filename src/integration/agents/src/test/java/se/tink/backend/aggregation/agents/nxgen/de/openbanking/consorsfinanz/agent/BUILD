load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "agent",
    srcs = glob(["*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
    ],
)
