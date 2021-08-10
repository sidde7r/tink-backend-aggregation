load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "eidassigner",
    srcs = glob(["*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidasidentity/identity",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/truststrategy",
        "//src/libraries/metrics:collection",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:gauges",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
        "@maven//:io_opentracing_opentracing_api",
        "@tink_backend//src/libraries/tracing/lib",
    ],
)
