load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "netty_versions_grpc_for_tls_ma_test",
    srcs = glob(
        ["*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/uuid",
        "//third_party:io_grpc_grpc_context",
        "//third_party:io_grpc_grpc_netty",
        "//third_party:io_grpc_grpc_protobuf_lite",
        "//third_party:io_grpc_grpc_stub",
        "//third_party:io_grpc_grpc_testing",
        "//third_party:io_grpc_grpc_testing_proto",
        "//third_party:io_netty_netty_codec_http2",
        "//third_party:io_netty_netty_common",
        "//third_party:io_netty_netty_handler",
        "//third_party:io_netty_netty_resolver",
        "//third_party:io_netty_netty_tcnative_boringssl_static",
        "//third_party:junit_junit",
        "//third_party:org_mockito_mockito_core",
        "//third_party/grpc",
        "@com_salesforce_servicelibs_grpc_testing_contrib",
    ],
)
