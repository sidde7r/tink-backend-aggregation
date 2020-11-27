load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "entercard",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/entercard",
        "//src/integration/amount",
        "//src/integration/lib",
        "//src/libraries/serialization",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_code_gson_gson",
    ],
)
