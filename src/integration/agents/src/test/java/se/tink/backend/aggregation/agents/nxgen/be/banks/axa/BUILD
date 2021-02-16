load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "axa",
    srcs = glob(["**/*.java"]),
    data = ["//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/axa/resources"],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/banks/axa",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/crypto",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/encoding",
        "//src/libraries/serialization",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcprov_jdk15on",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
