load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "aggregationcontroller",
    srcs = glob(["*.java"]),
    data = [
        "//src/aggregation/aggregationcontroller_api/src/test/java/se/tink/backend/aggregation/aggregationcontroller/resources",
    ],
    runtime_deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/entities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/parsing",
    ],
    deps = [
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller",
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller/iface",
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller/v1/core",
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller/v1/rpc",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/configuration",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock",
        "//third_party:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
    ],
)
