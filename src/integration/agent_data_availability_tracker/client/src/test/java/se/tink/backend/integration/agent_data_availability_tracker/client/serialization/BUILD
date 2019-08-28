load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "serialization-test",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/aggregationcontroller_api",
        "//src/integration/account:enums",
        "//src/integration/account:rpc",
        "//src/integration/agent_data_availability_tracker/client",
        "//src/integration/agent_data_availability_tracker/client/src/main/java/se/tink/backend/integration/agent_data_availability_tracker/client/serialization",
        "//src/integration/agent_data_availability_tracker/lib:client-lib",
        "//src/integration/api",
        "//src/integration/models",
        "//src/libraries/account_identifier",
        "//third_party:com_google_guava_guava",
    ],
)
