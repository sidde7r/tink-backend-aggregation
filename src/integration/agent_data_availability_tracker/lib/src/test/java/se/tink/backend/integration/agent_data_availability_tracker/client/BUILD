load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "transmitter-lib-test",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agent_data_availability_tracker/lib:client-lib",
        "@maven//:com_google_guava_guava",
    ],
)
