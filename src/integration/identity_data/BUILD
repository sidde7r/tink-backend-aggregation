load("@rules_codeowners//tools:codeowners.bzl", "codeowners", "generate_codeowners")
load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "identity_data",
    srcs = glob(["src/main/**/*.java"]),
    extdeps = [
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
    ],
    visibility = ["//visibility:public"],
)

junit_test(
    name = "identity_data_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/identity_data",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_google_guava_guava",
    ],
)

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-agent-platform-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
