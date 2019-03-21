java_library(
    name = "grpc-services",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//proto/integration_service:tink-grpc-integration",
        "//src/integration/lib", # TODO: remove this dependency
        "//src/integration/agents",
        "//third_party:com_google_inject_guice",
        "//src/libraries/metrics:metrics", # TODO: remove this dependency
    ]
)