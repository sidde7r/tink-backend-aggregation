load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "iface",
    srcs = glob(["**/*.java"]),
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/integration:__subpackages__",
    ],
    deps = [
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller/v1/core",
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller/v1/rpc",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation/rpc",
        "//third_party:com_sun_jersey_jersey_core",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/credentialsservice",
    ],
)
