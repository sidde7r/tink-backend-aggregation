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
        "@maven//:net_bytebuddy_byte_buddy",
        "@maven//:org_objenesis_objenesis",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/skandiabanken",
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/libraries/credentials:service",
        "//src/libraries/serialization",
        "@maven//:org_mockito_mockito_core",
    ],
)
