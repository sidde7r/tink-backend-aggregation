java_library(
    name = "common",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller/v1/rpc",
        "//src/integration/agent_data_availability_tracker/proto",
        "//src/integration/agent_data_availability_tracker/proto:java_grpc",
        "//src/integration/agent_data_availability_tracker/proto:java_proto",
        "//src/integration/lib:annotations",
        "//src/libraries/account_identifier",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:io_grpc_grpc_core",
        "//third_party:io_netty_netty_all",
        "//third_party:netty",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
    ],
)
