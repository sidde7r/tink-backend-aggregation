java_library(
    name = "metrics",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:registry",
        "//third_party:com_google_guava_guava",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/types/timers",
    ],
)
