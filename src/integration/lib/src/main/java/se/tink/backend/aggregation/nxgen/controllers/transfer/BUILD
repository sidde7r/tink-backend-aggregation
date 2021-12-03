load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "transfer",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/transfer",
        "//src/integration/transfer:rpc",
        "//src/libraries/agent-storage",
        "//third_party:com_google_guava_guava",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
