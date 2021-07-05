load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "luminor",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
        "//etc:development",
        "//:agent_test_data"
    ],
    visibility = ["//visibility:public"],
    deps = [
      "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/lv/openbanking/luminor",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "@tink_backend//src/libraries/account_identifier",
    ],
)