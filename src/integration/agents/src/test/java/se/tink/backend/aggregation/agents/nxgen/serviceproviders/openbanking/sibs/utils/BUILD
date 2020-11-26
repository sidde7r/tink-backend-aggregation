load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "utils",
    srcs = glob(["*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sibs",
        "//src/integration/amount",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
    ],
)
