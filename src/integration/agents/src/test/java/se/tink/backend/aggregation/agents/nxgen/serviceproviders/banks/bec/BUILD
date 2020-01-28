load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bec",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//etc:development",
    ],
    tags = [
        "external",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bec",
        "//src/integration/amount",
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/integration/models",
        "//src/libraries/serialization",
        "//third_party:org_mockito_mockito_core",
    ],
)
