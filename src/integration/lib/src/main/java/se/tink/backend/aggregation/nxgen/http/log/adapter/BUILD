load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "adapter",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/log",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/log/constants",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/log/executor",
        "//src/libraries/date",
        "//src/tink_backend_libs:har_logger",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:commons_io_commons_io",
        "//third_party:lombok_libs",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
