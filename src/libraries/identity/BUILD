load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "identity",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//:main-api", #todo, we don't want dependency towards main-api here.
        "//src/libraries/log:log",
        "//src/libraries/i18n:i18n",
        "//src/libraries/date:date",
        "//third_party:com_google_guava_guava",
    ],
)


junit_test(
    name = "identity-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":identity",

        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:joda_time_joda_time",
    ],
)
