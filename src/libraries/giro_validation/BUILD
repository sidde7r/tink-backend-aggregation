load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "giro_validation",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.giro.validation",
    visibility = ["//visibility:public"],
    deps = [
        # Guava
        "@maven//:com_google_guava_guava",

        # Stringutils
        "@maven//:org_apache_commons_commons_lang3",

        # JSR305 (javax.annotations)
        "@maven//:com_google_code_findbugs_jsr305",
    ],
)

junit_test(
    name = "giro_validation_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/giro_validation",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)
