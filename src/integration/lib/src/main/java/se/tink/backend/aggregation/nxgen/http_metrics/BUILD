load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "http_metrics",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/exceptions/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/engine",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filters",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filters/iface",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/libraries/metrics:registry",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/core",
    ],
)
