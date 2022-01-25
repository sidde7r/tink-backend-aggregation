load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "common",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filterable/request",
        "//src/libraries/agent-storage",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
