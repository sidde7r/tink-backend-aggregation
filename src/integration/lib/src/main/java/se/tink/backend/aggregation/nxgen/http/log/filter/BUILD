load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "filter",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/log/adapter",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/log/constants",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/log/executor",
        "//src/libraries/date",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:commons_io_commons_io",
        "//third_party:lombok_libs",
    ],
)
