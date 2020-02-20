load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

java_grpc_library(
    name = "sa-grpc-api",
    srcs = [":sa-agent-grpc-api-proto"],
    visibility = ["//visibility:public"],
    deps = [
        ":java_proto",
    ],
)

java_proto_library(
    name = "java_proto",
    visibility = ["//visibility:public"],
    deps = [":sa-agent-grpc-api-proto"],
)

proto_library(
    name = "sa-agent-grpc-api-proto",
    srcs = [
        "proto/auth/model.proto",
        "proto/auth/services.proto",
        "proto/common/io_common.proto",
        "proto/fetch/account/account.proto",
        "proto/fetch/account/model.proto",
        "proto/fetch/account/services.proto",
        "proto/fetch/trans/model.proto",
        "proto/fetch/trans/services.proto",
        "proto/security/model.proto",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "@com_google_googleapis//google/type:date_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)
