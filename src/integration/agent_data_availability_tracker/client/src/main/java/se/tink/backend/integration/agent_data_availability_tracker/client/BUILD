java_library(
    name = "client",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//proto/agent_data_availability_tracker:lib",
        "//src/aggregationcontroller_api",
        "//src/integration/agent_data_availability_tracker/client:serialization",
        "//src/integration/agent_data_availability_tracker/lib:client-lib",
        "//src/integration/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/models",
        "//src/libraries/account:enums",
        "//src/libraries/account:rpc",
        "//src/libraries/account_identifier",
        "@grpc//:io_grpc_grpc_netty",
        "@grpc//:io_netty_netty_handler",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
