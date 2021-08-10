load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

lib_srcs = ["BPostBankApiClientResponseMockFactory.java"]

java_library(
    name = "lib",
    testonly = True,
    srcs = lib_srcs,
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/banks/bpost:bpostbank",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib:annotations",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_json_json",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_objenesis_objenesis",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_google_guava_guava",
    ],
)

junit_test(
    name = "bpostbank",
    srcs = glob(
        ["**/*.java"],
        exclude = lib_srcs,
    ),
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/fakelogmasker",
    ],
    deps = [
        ":lib",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/common",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/banks/bpost:bpostbank",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib:annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filterable/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/wiremock",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_json_json",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_objenesis_objenesis",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_google_guava_guava",
    ],
)
