load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "assertions",
    srcs = glob([
        "*.java",
        "**/*.java",
    ]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/comparor",
        "//src/libraries/metrics:registry",
        "//src/libraries/strings",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:io_dropwizard_dropwizard_configuration",
        "//third_party:junit_junit",
        "//third_party:lombok_libs",
        "//third_party:lombok_plugin",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_projectlombok_lombok",
    ],
)
