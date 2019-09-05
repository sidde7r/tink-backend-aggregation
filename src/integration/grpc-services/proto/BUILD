load("@build_stack_rules_proto//java:java_grpc_library.bzl", "java_grpc_library")

java_grpc_library(
    name = "tink-grpc-integration",
    visibility = ["//visibility:public"],
    deps = [
        ":integration-proto",
    ],
)

proto_library(
    name = "integration-proto",
    srcs = [
        "models.proto",
        "rpc.proto",
        "services.proto",
    ],
)
