load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "belfius_auth",
    srcs = glob(["**/*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/framework",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/banks/belfius",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
    ],
)
