load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "swedbank",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/swedbank",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/consent/generators/se/swedbank",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/module/annotation",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner/module",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/agents/componentproviders",
        "//third_party:com_google_inject_guice",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/authentication_options",
    ],
)
