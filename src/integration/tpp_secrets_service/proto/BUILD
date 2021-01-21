load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

proto_library(
    name = "proto",
    srcs = glob([
        "service.proto",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "@com_google_protobuf//:empty_proto",
        "@com_google_protobuf//:struct_proto",
    ],
)

java_proto_library(
    name = "java_proto",
    visibility = ["//visibility:public"],
    deps = [":proto"],
)

java_grpc_library(
    name = "java_grpc",
    srcs = [":proto"],
    visibility = ["//visibility:public"],
    deps = [":java_proto"],
)
