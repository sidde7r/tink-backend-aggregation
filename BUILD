package(default_visibility = ["//visibility:public"])

alias(
    name = "aggregation",
    actual = "//src/aggregation/service:bin",
)

alias(
    name = "convert_s3_to_aap",
    actual = "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/converter",
)

# Files needed by manual agent tests
filegroup(
    name = "agent_test_data",
    srcs = [
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
)

test_suite(
    name = "most_tests",
    tests = [
        "//src/aggregation/api:api_test",
        "//src/aggregation/lib/src/test/java/se/tink/backend/aggregation/wrappers",
        "//src/integration/lib:lib_test",
    ],
)

alias(
    name = "format",
    actual = "//tools/format:format-java-diff",
)

alias(
    name = "formatjson",
    actual = "//tools/format:format-json-diff",
)

# Workaround for annoying "no such target '//:.bazelproject'" error in recent plugin versions
exports_files([".bazelproject"])
