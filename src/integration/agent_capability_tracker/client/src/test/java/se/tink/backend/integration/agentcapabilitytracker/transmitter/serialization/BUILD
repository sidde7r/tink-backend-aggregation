load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "serialization-test",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregationcontroller_api",
        "//src/integration/agent_capability_tracker/client/src/main/java/se/tink/backend/integration/agentcapabilitytracker/transmitter/serialization",
        "//src/integration/agent_capability_tracker/lib:transmitter-lib",
        "//src/integration/api",
        "//src/integration/models",
        "//src/libraries/account:enums",
        "//src/libraries/account:rpc",
        "//src/libraries/account_identifier",
        "//third_party:com_google_guava_guava",
    ],
)
