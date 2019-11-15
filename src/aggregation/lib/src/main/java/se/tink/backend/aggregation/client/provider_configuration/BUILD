java_library(
    name = "provider_configuration",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/api/src/main/java/se/tink/backend/aggregation/cluster/identification",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/client/provider_configuration/rpc",
        "//src/libraries/api_annotations",
        "//src/libraries/http:http_annotations",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:io_dropwizard_dropwizard_core",
    ],
)
