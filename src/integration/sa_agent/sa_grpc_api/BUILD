load("@build_stack_rules_proto//java:java_grpc_library.bzl", "java_grpc_library")

java_grpc_library(
    name = "sa-grpc-api",
    visibility = ["//visibility:public"],
    deps = [
        ":sa-agent-grpc-api-proto",
    ],
)

proto_library(
    name = "sa-agent-grpc-api-proto",
    srcs = [
        "proto/auth/model.proto",
        "proto/auth/services.proto",
        "proto/fetch/account/model.proto",
        "proto/fetch/account/services.proto",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "@com_google_googleapis//google/type:date_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)
