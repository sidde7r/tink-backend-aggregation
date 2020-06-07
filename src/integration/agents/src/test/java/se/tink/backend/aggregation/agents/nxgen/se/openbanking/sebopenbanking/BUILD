load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "sebopenbanking",
    srcs = glob(["*.java"]),
    data = ["//:agent_test_data"],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/sebopenbanking",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/sebopenbanking",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//third_party:org_mockito_mockito_core",
    ],
)
