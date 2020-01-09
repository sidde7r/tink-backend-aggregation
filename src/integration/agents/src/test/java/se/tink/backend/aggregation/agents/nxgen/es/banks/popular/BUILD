load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "popular",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:agent_unit_tests",
        "//data:aggregation_test",
        "//data/test/agents/es/bankinter",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/es/banks/popular",
        "//src/integration/api",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:org_mockito_mockito_core",
    ],
)
