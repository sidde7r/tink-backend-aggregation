load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "tpp-secrets-service-client-test",
    srcs = glob(["**/*.java"]),
    data = [
    ],
    deps = [
        "//src/integration/tpp_secrets_service/client:entities",
        "//src/integration/tpp_secrets_service/client/src/main/java/se/tink/backend/integration/tpp_secrets_service/client:tpp-secrets-service-client",
        "//src/integration/tpp_secrets_service/proto:java_grpc",
        "//src/integration/tpp_secrets_service/proto:java_proto",
        "//third_party:org_mockito_mockito_core",
        "@io_grpc_grpc_java//api",
        "@io_grpc_grpc_java//stub",
    ],
)
