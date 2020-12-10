load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "module",
    srcs = glob(
        ["*.java"],
    ),
    data = [
        "//data:qsealc_crt",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agent_data_availability_tracker/client",
        "//src/integration/agent_data_availability_tracker/client/src/main/java/se/tink/backend/integration/agent_data_availability_tracker/common",
        "//src/integration/agent_data_availability_tracker/client/src/main/java/se/tink/backend/integration/agent_data_availability_tracker/module:agent-data-availability-tracker-module",
        "//src/libraries/serialization",
        "//third_party:com_google_inject_guice",
    ],
)
