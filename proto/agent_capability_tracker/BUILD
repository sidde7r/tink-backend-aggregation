proto_library(
    name = "proto",
    srcs = glob([
        "services.proto",
    ]),
    visibility = ["//visibility:public"],
)

load("@build_stack_rules_proto//java:java_grpc_library.bzl", "java_grpc_library")

java_grpc_library(
    name = "lib",
    visibility = ["//visibility:public"],
    deps = [
        ":proto",
    ],
)
