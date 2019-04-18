load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "i18n",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.i18n",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:com_googlecode_gettext_commons_gettext_commons",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "i18n_test",
    srcs = glob(["src/test/**/*.java"]),
    runtime_deps = [
        "@maven//:asm_asm",
        "@maven//:net_bytebuddy_byte_buddy",
        "@maven//:org_objenesis_objenesis",
    ],
    deps = [
        "//src/libraries/i18n",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_mockito_mockito_core",
    ],
)
