load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "service_version",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/metrics:collection",
        "//src/libraries/metrics:configuration",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:counters",
        "//src/libraries/metrics:gauges",
        "//src/libraries/metrics:histograms",
        "//src/libraries/metrics:logbackappender",
        "//src/libraries/metrics:monitor",
        "//src/libraries/metrics:other",
        "//src/libraries/metrics:prometheus",
        "//src/libraries/metrics:registry",
        "//src/libraries/metrics:timers",
        "//src/libraries/serialization",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_guice",
        "@aggregation//:com_google_guava_guava",
    ],
)
