load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "spankki",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/crosskey",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/models",
        "//third_party:com_google_inject_guice",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
