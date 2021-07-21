load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "module",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bpcegroup",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/module/loader",
        "//third_party:com_google_inject_guice",
    ],
)
