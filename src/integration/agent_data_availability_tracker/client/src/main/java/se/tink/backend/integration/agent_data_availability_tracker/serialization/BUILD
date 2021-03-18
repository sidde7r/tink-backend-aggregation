java_library(
    name = "serialization",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller/v1/rpc",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib:annotations",
        "//src/integration/models",
        "//third_party:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/agent-data-availability-tracker/src/main/java/se/tink/backend/agent_data_availability_tracker/client",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
