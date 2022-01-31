load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "lacaixa",
    srcs = glob(
        include = ["**/*.java"],
    ),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/caixa/utils",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)
