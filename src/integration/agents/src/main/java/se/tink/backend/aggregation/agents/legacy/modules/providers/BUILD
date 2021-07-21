load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "providers",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration/provider/socket",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/jersey",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/libraries/net/src/main/java/se/tink/libraries/net/client",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:commons_codec_commons_codec",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
