load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "unicredit",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/unicredit",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup/payment",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/supplementalfields",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/api:psd2_headers",
        "//src/integration/payment:enums",
        "//src/integration/payment:rpc",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang3",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
        "@org_slf4j_slf4j_api",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
