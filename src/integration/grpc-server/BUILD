java_library(
    name = "grpc-server",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/grpc-services",
        "//src/libraries/api_annotations",
        "//src/libraries/metrics",
        "//third_party:bouncycastle",
        "//third_party:com_google_inject_extensions_guice_multibindings",
        "//third_party:log",
        "//third_party:netty",
        "//third_party:prometheus",
    ],
)
