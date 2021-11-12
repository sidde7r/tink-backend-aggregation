load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "labanquepostale_authenticator",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/labanquepostale/integration/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = ["//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/labanquepostale"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/labanquepostale",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/utils",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)
