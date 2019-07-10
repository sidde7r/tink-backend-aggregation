load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "transmitter-lib-test",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agent_capability_tracker/lib:transmitter-lib",
        "//third_party:com_google_guava_guava",
    ],
)
