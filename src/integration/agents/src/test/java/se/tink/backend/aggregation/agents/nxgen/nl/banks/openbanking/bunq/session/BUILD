load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "session",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/bunq/session/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/bunq",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bunq",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/libraries/agent-storage",
        "//src/libraries/serialization",
        "//third_party:io_vavr",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
