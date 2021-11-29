load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "to_system",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/typeguesser/accountholder",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/account",
        "//src/integration/models",
        "//src/integration/user:rpc",
        "//src/libraries/date",
        "//third_party:com_google_guava_guava",
        "//third_party:org_slf4j_slf4j_api",
        "@tink_backend//src/libraries/serialization_utils",
    ],
)
