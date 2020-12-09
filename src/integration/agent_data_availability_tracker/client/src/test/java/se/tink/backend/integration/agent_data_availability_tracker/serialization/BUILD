load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "serialization-test",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller/v1/rpc",
        "//src/integration/account/src/main/java/se/tink/libraries/account/enums",
        "//src/integration/agent_data_availability_tracker/client",
        "//src/integration/agent_data_availability_tracker/client/src/main/java/se/tink/backend/integration/agent_data_availability_tracker/common",
        "//src/integration/agent_data_availability_tracker/client/src/main/java/se/tink/backend/integration/agent_data_availability_tracker/serialization",
        "//src/integration/agent_data_availability_tracker/lib:client-lib",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/models",
        "//src/libraries/account_identifier",
        "@aggregation//:com_google_guava_guava",
    ],
)
