java_library(
    name = "grpc-services",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/grpc-services/proto:tink-grpc-integration",
        "//src/integration/lib",  # TODO: remove this dependency
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
        "//src/libraries/metrics:timers",  # TODO: remove this dependency
        "//third_party:com_google_inject_guice",
    ],
)
