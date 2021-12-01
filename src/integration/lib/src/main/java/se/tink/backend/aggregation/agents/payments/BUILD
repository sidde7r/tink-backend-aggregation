load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "payments",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/capability",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/transfer",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/payment",
        "//src/integration/payment:rpc",
        "//src/integration/transfer:rpc",
    ],
)
