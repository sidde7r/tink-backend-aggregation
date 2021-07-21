load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")
load("@rules_java//java:defs.bzl", "java_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

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
    name = "java_grpc",
    srcs = [":proto"],
    visibility = ["//visibility:public"],
    deps = [":java_proto"],
)
