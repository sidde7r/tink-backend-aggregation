java_library(
    name = "grpc-server",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/grpc-services",
        "//src/libraries/api_annotations",
        "//src/libraries/metrics",
        "//third_party:log",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_extensions_guice_multibindings",
        "@maven//:com_google_inject_guice",
        "@maven//:io_grpc_grpc_core",
        "@maven//:io_grpc_grpc_netty",
        "@maven//:io_prometheus_simpleclient",
        "@maven//:org_bouncycastle_bcpkix_jdk15on",
        "@maven//:org_bouncycastle_bcprov_jdk15on",
    ],
)
