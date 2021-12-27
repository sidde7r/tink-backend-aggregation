load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "http_api_client",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas/proxy",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidasidentity/identity",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/configuration",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/constants",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/log/executor",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http_api_client/variable_detection/storage",
        "//src/libraries/agent-storage",
        "//src/tink_backend_libs:aggregation_agent_api_client",
        "//third_party:lombok_libs",
        "@aggregation//:com_google_guava_guava",
    ],
)

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/agent-effectiveness-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
