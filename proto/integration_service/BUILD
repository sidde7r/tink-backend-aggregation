load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

java_proto_library(
    name = "tink-grpc-integration",
    protos = [
        "rpc.proto",
        "services.proto",
    ],
    visibility = ["//visibility:public"],
    with_grpc = True,
)
