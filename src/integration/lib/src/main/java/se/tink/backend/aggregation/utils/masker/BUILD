load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "masker",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/crypto/hash",
        "//src/libraries/serialization",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
    ],
)
