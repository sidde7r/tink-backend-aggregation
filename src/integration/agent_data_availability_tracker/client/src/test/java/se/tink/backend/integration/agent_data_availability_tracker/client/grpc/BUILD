load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "grpc",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agent_data_availability_tracker/client",
        "//src/integration/agent_data_availability_tracker/lib:client-lib",
        "//src/integration/agent_data_availability_tracker/proto:java_grpc",
        "//src/integration/agent_data_availability_tracker/proto:java_proto",
        "//src/integration/api",
        "//src/integration/models",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_extensions_guice_multibindings",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_lifecycle",
        "//third_party:io_grpc_grpc_netty",
        "//third_party:log",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_bouncycastle_bcprov_jdk15on",
        "@io_grpc_grpc_java//api",
        "@io_grpc_grpc_java//stub",
    ],
)
