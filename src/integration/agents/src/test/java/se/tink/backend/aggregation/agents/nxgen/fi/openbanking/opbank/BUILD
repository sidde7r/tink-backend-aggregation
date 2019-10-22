load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "opbank",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:integration_agents_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:ch_qos_logback_logback_classic",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        "//src/integration/agents",
        "//src/integration/agents:agents_test",
        "//third_party:tink_backend_integration_framework",
    ],
)
