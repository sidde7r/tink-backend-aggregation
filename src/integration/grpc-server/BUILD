java_library(
    name = "grpc-server",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:netty",
        "//third_party:prometheus",
        "//third_party:log",
        "//third_party:bouncycastle",
        "//src/integration/grpc-services:grpc-services",
        "//third_party:com_google_inject_extensions_guice_multibindings",
    ],
)
