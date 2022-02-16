load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "impl",
    srcs = glob(
        include = ["*.java"],
    ),
    data = [
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
    ],
    deps = [
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/payment/validation/impl",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/utils",
        "//src/integration/payment:rpc",
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation/enums",
        "//src/integration/transfer:rpc",
        "//third_party:junit_junit",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@tink_backend//src/libraries/payments_common/src/java/se/tink/libraries/payments/common/model:payments_common_model",
    ],
)
