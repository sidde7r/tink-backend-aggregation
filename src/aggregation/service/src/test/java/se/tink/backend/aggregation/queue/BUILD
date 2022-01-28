load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "queue",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/queue",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/agents/componentproviders",
        "//src/libraries/queue_sqs",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_inject_guice",
        "//third_party:org_mockito_mockito_core",
    ],
)
