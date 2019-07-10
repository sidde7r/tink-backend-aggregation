load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "transmitter-client-test",
    srcs = glob(
        ["**/*.java"],
    ),
    data = [
        "//etc/integration:development-configuration",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregationcontroller_api",
        "//src/integration/agent_capability_tracker/client:transmitter-client",
        "//src/integration/agent_capability_tracker/lib:transmitter-lib",
        "//src/integration/api",
        "//src/integration/models",
        "//src/libraries/account:enums",
        "//src/libraries/account:rpc",
        "//src/libraries/account_identifier",
    ],
)
