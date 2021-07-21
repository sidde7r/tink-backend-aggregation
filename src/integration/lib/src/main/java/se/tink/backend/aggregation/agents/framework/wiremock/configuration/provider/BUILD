load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "provider",
    srcs = glob(["*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
    ],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration/provider/socket",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:javax_inject_javax_inject",
    ],
)
