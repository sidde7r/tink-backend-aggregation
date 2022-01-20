load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")
load("@rules_java//java:defs.bzl", "java_library", "java_proto_library")

java_library(
    name = "utils",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "java_grpc",
        "java_proto",
        "//src/integration/tpp_secrets_service/client/src/main/java/se/tink/backend/integration/tpp_secrets_service/client/entities",
    ],
)

java_grpc_library(
    name = "java_grpc",
    srcs = [
        "@tink_backend//src/tink-integration-eidas-secrets-service/proto",
    ],
    deps = [":java_proto"],
)

java_proto_library(
    name = "java_proto",
    deps = [
        "@tink_backend//src/tink-integration-eidas-secrets-service/proto",
    ],
)
