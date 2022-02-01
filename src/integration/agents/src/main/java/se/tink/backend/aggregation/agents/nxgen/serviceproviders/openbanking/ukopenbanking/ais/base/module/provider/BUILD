load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "provider",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base/configuration",
        "//src/integration/lib",
        "//third_party:com_google_inject_guice",
    ],
)
