java_library(
    name = "serialization",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller/v1/rpc",
        "//src/integration/agent_data_availability_tracker/lib:client-lib",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib:annotations",
        "//src/integration/models",
        "//src/libraries/account_identifier",
        "//third_party:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_guava_guava",
    ],
)
