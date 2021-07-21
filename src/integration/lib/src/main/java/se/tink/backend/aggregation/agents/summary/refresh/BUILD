load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "refresh",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/models",
        "//src/integration/user:rpc",
        "//third_party:com_google_code_gson_gson",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
