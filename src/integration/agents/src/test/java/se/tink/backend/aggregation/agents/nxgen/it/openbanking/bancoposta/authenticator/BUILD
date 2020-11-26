load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bancoposta_authenticator",
    srcs = glob(["**/*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/bancoposta",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
