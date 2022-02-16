load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "deutschebank",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/deutschebank",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/enums",
        "//src/integration/lib",
        "//src/integration/lib:annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup/payment",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/payment/validation/impl",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/libraries/agent-storage",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
