load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

proto_library(
    name = "proto",
    srcs = glob([
        "services.proto",
    ]),
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "java_proto",
    visibility = ["//visibility:public"],
    deps = [":proto"],
)

java_grpc_library(
    name = "lib",
    srcs = [":proto"],
    visibility = ["//visibility:public"],
    deps = [":java_proto"],
)
