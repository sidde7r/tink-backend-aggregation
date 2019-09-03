load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "skandiabanken",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:agent_unit_tests",
        "//data:aggregation_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/skandiabanken",
        "//src/integration/api",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/libraries/serialization",
        "//third_party:org_mockito_mockito_core",
    ],
)
