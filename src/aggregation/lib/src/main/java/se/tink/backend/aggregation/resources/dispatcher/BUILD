load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "dispatcher",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/api/src/main/java/se/tink/backend/aggregation/cluster/identification",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/queue",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/worker",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/libraries/queue_lib",
        "//third_party:com_google_inject_guice",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/core",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/registry",
    ],
)
