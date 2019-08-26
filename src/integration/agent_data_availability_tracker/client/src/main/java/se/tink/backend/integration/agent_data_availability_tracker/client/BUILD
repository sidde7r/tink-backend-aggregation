java_library(
    name = "agent-data-availability-tracker-client",
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
        "//src/integration/lib:annotations",
        "//src/integration/models",
        "//src/libraries/account:enums",
        "//src/libraries/account:rpc",
        "//src/libraries/account_identifier",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_lifecycle",
        "//third_party:io_netty_netty_all",
        "//third_party:netty",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
