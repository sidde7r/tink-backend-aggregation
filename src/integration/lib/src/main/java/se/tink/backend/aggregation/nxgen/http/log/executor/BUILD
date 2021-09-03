load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "executor",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/log",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/log/constants",
        "//src/libraries/date",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
