load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "giro_validation",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.giro.validation",
    visibility = ["//visibility:public"],
    deps = [
        # Guava
        "//third_party:com_google_guava_guava",

        # Stringutils
        "//third_party:org_apache_commons_commons_lang3",

        # JSR305 (javax.annotations)
        "//third_party:com_google_code_findbugs_jsr305",
    ],
)

junit_test(
    name = "giro_validation_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/giro_validation",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
