load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "i18n",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_inject_guice",
        "//third_party:com_googlecode_gettext_commons_gettext_commons",
        "//third_party:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "i18n_test",
    srcs = glob(["src/test/**/*.java"]),
    runtime_deps = [
        "//third_party:asm_asm",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        "//src/libraries/i18n",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
