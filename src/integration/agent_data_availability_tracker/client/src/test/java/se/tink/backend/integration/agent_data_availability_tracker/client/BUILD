load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "agent-data-availability-tracker-client-test",
    srcs = glob(
        ["**/*.java"],
    ),
    data = [
        "//etc/integration:development-configuration",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregationcontroller_api",
        "//src/integration/account:enums",
        "//src/integration/account:rpc",
        "//src/integration/agent_data_availability_tracker/client",
        "//src/integration/agent_data_availability_tracker/lib:client-lib",
        "//src/integration/api",
        "//src/integration/models",
        "//src/libraries/account_identifier",
        "//third_party:io_dropwizard_dropwizard_lifecycle",
    ],
)
