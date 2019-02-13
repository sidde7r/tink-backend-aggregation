package(default_visibility = ["//visibility:public"])

alias(
    name = "aggregation",
    actual = "//src/aggregation/service:aggregation",
)

alias(
    name = "provider_configuration",
    actual = "//src/provider_configuration/service:provider_configuration",
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
