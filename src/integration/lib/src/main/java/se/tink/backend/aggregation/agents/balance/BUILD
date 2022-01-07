load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "calculation",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/models",
        "//src/libraries/account_data_cache",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_google_guava_guava",
    ],
)
