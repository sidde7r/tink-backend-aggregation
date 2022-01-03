load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")
load("@rules_java//java:defs.bzl", "java_library", "java_proto_library")

java_library(
    name = "aggregation_agent_api_client",
    srcs = ["@tink_backend//src/libraries/aggregation_agent_api_client:aggregation_agent_api_client_srcjar"],
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
        "//third_party:com_google_guava_guava",
        "//third_party:javax_validation_validation_api",
        "//third_party:lombok_plugin",
        "//third_party:org_hibernate_hibernate_validator",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:org_projectlombok_lombok",
        "@maven//:com_squareup_okhttp3_okhttp",
        "@maven//:com_squareup_okhttp3_okhttp_tls",
        "@maven//:com_squareup_okio_okio",
        "@maven//:javax_el_javax_el_api",
    ],
)

java_library(
    name = "har_logger",
    srcs = ["@tink_backend//src/libraries/har_logger:har_logger_srcjar"],
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:lombok_plugin",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:org_projectlombok_lombok",
    ],
)

java_library(
    name = "secrets_service_client",
    srcs = ["@tink_backend//src/tink-integration-eidas-secrets-service/client:client_srcjar"],
    visibility = ["//visibility:public"],
    deps = [
        "ss_java_grpc",
        "ss_java_proto",
        "//third_party:com_google_guava_guava",
        "//third_party:io_grpc_grpc_protobuf",
        "//third_party/failsafe",
        "//third_party/log4j",
        "@com_google_protobuf//:protobuf_java",
        "@io_grpc_grpc_java//core",
        "@maven//:io_prometheus_simpleclient",
        "@maven//:net_jodah_failsafe",
        "@tink_backend//src/tink-integration-eidas-secrets-service/client/configuration",
        "@tink_backend//src/tink-integration-eidas-secrets-service/client/lib",
    ],
)

java_grpc_library(
    name = "ss_java_grpc",
    srcs = [
        "@tink_backend//src/tink-integration-eidas-secrets-service/proto",
    ],
    deps = [":ss_java_proto"],
)

java_proto_library(
    name = "ss_java_proto",
    deps = [
        "@tink_backend//src/tink-integration-eidas-secrets-service/proto",
    ],
)
