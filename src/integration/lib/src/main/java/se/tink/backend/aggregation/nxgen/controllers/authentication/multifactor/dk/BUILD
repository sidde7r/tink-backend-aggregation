load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "dk",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/log",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/supplementalfields",
        "//third_party:com_google_guava_guava",
        "//third_party:lombok_libs",
        "@org_slf4j_slf4j_api",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
    ],
)
