load("@build_stack_rules_proto//java:java_grpc_library.bzl", "java_grpc_library")

proto_library(
    name = "proto",
    srcs = glob([
        "service.proto",
    ]),
    visibility = ["//visibility:public"],
)

java_grpc_library(
    name = "lib",
    visibility = ["//visibility:public"],
    deps = [
        ":proto",
    ],
)
