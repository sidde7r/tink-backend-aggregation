load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "logs",
    srcs = glob(["**/*.java"]),
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/configuration/models/configuration",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/logs",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/context",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/agents/componentproviders",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/log/executor",
        "//third_party:com_amazonaws_aws_java_sdk_s3",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)
