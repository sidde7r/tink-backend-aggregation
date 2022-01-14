load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "options",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//third_party:lombok_libs",
        "//third_party:org_reflections_reflections",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/authentication_options",
    ],
)
