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
        "//third_party:netty",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_bouncycastle_bcprov_jdk15on",
        "//third_party/failsafe",
        "//third_party/log4j",
        "@com_google_api_grpc_proto_google_common_protos//jar",
        "@com_google_protobuf//:protobuf_java",
        "@io_grpc_grpc_java//core",
        "@io_grpc_grpc_services//jar",
        "@maven//:com_squareup_okhttp3_okhttp",
        "@maven//:io_grpc_grpc_netty_shaded",
        "@maven//:io_prometheus_simpleclient",
        "@maven//:net_jodah_failsafe",
        "@org_slf4j_slf4j_api//jar",
    ],
)

java_library(
    name = "qsealc_http_signer_client",
    srcs = ["@tink_backend//src/tink-integration-eidas-proxy/client:client_srcjar"],
    visibility = ["//visibility:public"],
    deps = [
        "eidas_cryptography_parsing",
        "eidas_dev_issuer_client",
        "//third_party/log4j",
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_errorprone_error_prone_annotations",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_nimbusds_nimbus_jose_jwt",
        "@aggregation//:org_apache_httpcomponents_httpclient",
        "@aggregation//:org_apache_httpcomponents_httpcore",
        "@aggregation//:org_bouncycastle_bcpkix_jdk15on",
        "@aggregation//:org_bouncycastle_bcprov_jdk15on",
        "@maven//:io_prometheus_simpleclient",
        "@maven//:org_apache_httpcomponents_client5_httpclient5",
        "@maven//:org_apache_httpcomponents_core5_httpcore5",
        "@org_slf4j_slf4j_api//jar",
    ],
)

java_library(
    name = "eidas_dev_issuer_client",
    srcs = ["@tink_backend//src/eidas-dev-issuer/client:client_srcjar"],
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "@aggregation//:org_apache_httpcomponents_httpclient",
        "@aggregation//:org_apache_httpcomponents_httpcore",
        "@aggregation//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "eidas_cryptography_parsing",
    srcs = ["@tink_backend//src/libraries/cryptography/src/main/java/tink/libraries/cryptography/parsing:parsing_srcjar"],
    visibility = ["//visibility:public"],
    deps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_bouncycastle_bcpkix_jdk15on",
        "@aggregation//:org_bouncycastle_bcprov_jdk15on",
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
