load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "belfius",
    srcs = glob(["**/*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/account/src/main/java/se/tink/libraries/account/enums",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/belfius",
        "//src/integration/lib",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_mockito_mockito_core",
    ],
)
