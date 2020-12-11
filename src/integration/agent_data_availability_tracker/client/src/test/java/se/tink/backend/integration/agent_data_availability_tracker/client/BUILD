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
        "//src/integration/account/src/main/java/se/tink/libraries/account/enums",
        "//src/integration/agent_data_availability_tracker/client",
        "//src/integration/agent_data_availability_tracker/client/src/main/java/se/tink/backend/integration/agent_data_availability_tracker/common",
        "//src/integration/agent_data_availability_tracker/client/src/main/java/se/tink/backend/integration/agent_data_availability_tracker/module:agent-data-availability-tracker-module",
        "//src/integration/agent_data_availability_tracker/client/src/main/java/se/tink/backend/integration/agent_data_availability_tracker/serialization",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/models",
        "//src/libraries/account_identifier",
        "//src/libraries/serialization",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_lifecycle",
        "@io_grpc_grpc_java//api",
    ],
)
