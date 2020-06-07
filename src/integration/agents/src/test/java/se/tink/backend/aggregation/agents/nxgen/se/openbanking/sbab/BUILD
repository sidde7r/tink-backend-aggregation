load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "sbab",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/sbab",
        "//src/libraries/date",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_mockito_mockito_core",
    ],
)
