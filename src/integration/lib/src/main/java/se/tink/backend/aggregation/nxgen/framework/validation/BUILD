load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "validation",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/identity_data",
        "//src/integration/models",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
