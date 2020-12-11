load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "individual_refresh",
    srcs = glob(["*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
    ],
    deps = [
        "//src/integration/account/src/main/java/se/tink/libraries/account/rpc",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/controllers",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
