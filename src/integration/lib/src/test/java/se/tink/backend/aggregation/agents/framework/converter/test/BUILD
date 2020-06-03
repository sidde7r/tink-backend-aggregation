load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "test",
    srcs = glob(
        ["*.java"],
    ),
    data = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/converter/test/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/utils/parsingrules",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/utils/parsingrules/parsingoperations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/utils/parsingrules/predicates",
    ],
    deps = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/converter",
    ],
)
