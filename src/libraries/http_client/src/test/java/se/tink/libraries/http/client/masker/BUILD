load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "masker",
    srcs = glob(["**/*.java"]),
    data = [
        "//src/libraries/http_client/src/test/java/se/tink/libraries/http/client/masker/resources",
    ],
    deps = [
        "//src/libraries/http_client/src/main/java/se/tink/libraries/http/client/masker",
        "//src/libraries/request_tracing",
        "//third_party:ch_qos_logback_logback_classic",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
