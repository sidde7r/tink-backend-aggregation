package(default_visibility = ["//visibility:public"])

alias(
    name = "aggregation",
    actual = "//src/aggregation/service:bin",
)

alias(
    name = "integration_framework",
    actual = "//src/integration/lib:framework",
)

alias(
    name = "ukob_register",
    actual = "//src/commands/ukob_register",
)

test_suite(
    name = "most_tests",
    tests = [
        "//src/aggregation/api:api_test",
        "//src/aggregation/lib:lib_test",
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
