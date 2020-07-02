load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "entities",
    srcs = glob(["*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sibs",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)
