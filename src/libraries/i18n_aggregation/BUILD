load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "i18n_aggregation",
    srcs = glob(["src/main/**/*.java"]),
    extdeps = [
        "//po:i18n_aggregation",
        "//third_party:com_google_inject_guice",
        "//third_party:com_googlecode_gettext_commons_gettext_commons",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
    ],
    visibility = ["//visibility:public"],
)

junit_test(
    name = "i18n_test",
    srcs = glob(["src/test/**/*.java"]),
    runtime_deps = [
        "//third_party:asm_asm",
    ],
    deps = [
        "i18n_aggregation",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
