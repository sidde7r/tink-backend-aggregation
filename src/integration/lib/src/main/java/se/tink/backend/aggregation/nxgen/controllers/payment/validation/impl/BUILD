load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "impl",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/payment/validation",
        "//src/integration/payment:enums",
        "//src/integration/payment:rpc",
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation/enums",
        "//src/integration/transfer:rpc",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/payments_common/src/java/se/tink/libraries/payments/common/model:payments_common_model",
    ],
)
