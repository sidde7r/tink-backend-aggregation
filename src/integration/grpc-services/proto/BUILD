load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

java_grpc_library(
    name = "tink-grpc-integration",
    srcs = [":integration-proto"],
    visibility = ["//visibility:public"],
    deps = [
        ":java_proto",
    ],
)

java_proto_library(
    name = "java_proto",
    visibility = ["//visibility:public"],
    deps = [":integration-proto"],
)

proto_library(
    name = "integration-proto",
    srcs = [
        "models.proto",
        "rpc.proto",
        "services.proto",
    ],
    visibility = ["//visibility:public"],
)
