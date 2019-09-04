java_library(
    name = "grpc-services",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents",
        "//src/integration/grpc-services/proto:tink-grpc-integration",
        "//src/integration/lib",  # TODO: remove this dependency
        "//src/libraries/metrics",  # TODO: remove this dependency
        "//third_party:com_google_inject_guice",
    ],
)
