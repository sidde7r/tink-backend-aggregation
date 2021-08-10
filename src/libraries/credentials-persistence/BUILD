load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "credentials-persistence",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/serializecontainer",
        "//src/libraries/agent-storage",
        "//src/libraries/serialization",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
