load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "consorsfinanz",
    srcs = glob(
        ["**/*.java"],
        exclude = ["agent/**/*.java"],
    ),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/consorsfinanz/resources",
    ],
    tags = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/consorsfinanz",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)
