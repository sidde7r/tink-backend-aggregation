load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "v62",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/client/provider_configuration/rpc",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/creditcards/amex/v62",
        "//src/integration/lib",
        "//src/integration/lib:annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/libraries/date",
        "//third_party:com_google_inject_guice",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
    ],
)
