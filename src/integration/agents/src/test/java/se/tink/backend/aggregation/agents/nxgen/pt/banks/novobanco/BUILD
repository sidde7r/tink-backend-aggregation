load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "novobanco",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//etc:development",
    ],
    tags = [
        "external",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/pt/banks/novobanco",
        "//src/integration/amount",
        "//src/integration/api",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/libraries/account_identifier",
        "//src/libraries/pair",
        "//src/libraries/serialization",
        "//third_party:ch_qos_logback_logback_classic",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_objenesis_objenesis",
    ],
)
