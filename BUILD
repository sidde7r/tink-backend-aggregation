package(default_visibility = ["//visibility:public"])

alias(
    name = "aggregation",
    actual = "//src/aggregation/service:bin",
)

alias(
    name = "provider_configuration",
    actual = "//src/provider_configuration/service:bin",
)

alias(
    name = "integration_framework",
    actual = "//src/integration/lib:framework",
)

alias(
    name = "ukob_register",
    actual = "//src/ukob_register",
)

test_suite(
    name = "all_tests",
    tests = [
        "//src/aggregation/api:api_test",
        "//src/aggregation/lib:lib_test",
        "//src/integration/lib:lib_test",
        "//src/provider_configuration/lib:lib_test",
    ],
)
