java_library(
    name = "log",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/api/src/main/java/se/tink/backend/aggregation/cluster/identification",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/user:rpc",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:org_slf4j_slf4j_api",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
    ],
)
