load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

java_proto_library(
    name = "tink-grpc-integration",
    protos = [
        "models.proto",
        "rpc.proto",
        "services.proto",
    ],
    visibility = ["//visibility:public"],
    with_grpc = True,
)
