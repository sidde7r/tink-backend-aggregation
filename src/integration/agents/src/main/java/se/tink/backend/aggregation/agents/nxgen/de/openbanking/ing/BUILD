load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "ing",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ingbase",
        "//src/integration/credentials:service",
        "//src/integration/enums",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup/payment",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/payment/validation/impl",
        "//third_party:com_google_inject_guice",
    ],
)
