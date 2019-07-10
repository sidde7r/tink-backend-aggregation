java_library(
    name = "transmitter-client",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//proto/agent_capability_tracker:lib",
        "//src/aggregationcontroller_api",
        "//src/integration/agent_capability_tracker/client/src/main/java/se/tink/backend/integration/agentcapabilitytracker/transmitter/serialization",
        "//src/integration/agent_capability_tracker/lib:transmitter-lib",
        "//src/integration/api",
        "//src/integration/models",
        "//src/libraries/account:enums",
        "//src/libraries/account:rpc",
        "//src/libraries/account_identifier",
        "//third_party:netty",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
