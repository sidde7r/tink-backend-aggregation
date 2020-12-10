java_library(
    name = "agent-data-availability-tracker-client",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agent_data_availability_tracker/client/src/main/java/se/tink/backend/integration/agent_data_availability_tracker/common",
        "//src/integration/agent_data_availability_tracker/proto",
        "//src/integration/agent_data_availability_tracker/proto:java_grpc",
        "//src/integration/agent_data_availability_tracker/proto:java_proto",
        "//third_party:com_google_inject_guice",
        "//third_party:org_slf4j_slf4j_api",
        "@com_google_guava_guava",
        "@io_grpc_grpc_api",
        "@io_grpc_grpc_java//stub",
        "@tink_backend//src/libraries/dropwizard_lifecycle",
    ],
)
