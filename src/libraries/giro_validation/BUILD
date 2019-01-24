load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "giro-validation",
    srcs = glob(["src/main/**/*.java"]),
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
    name = "giro-validataion-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":giro-validation",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
