load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "agent-data-availability-tracker-client-lib-test",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agent_data_availability_tracker/lib:client-lib",
        "@aggregation//:com_google_guava_guava",
    ],
)
