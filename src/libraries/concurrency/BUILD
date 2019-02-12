load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "concurrency",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/metrics",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "concurrency_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/concurrency",
        "//src/libraries/executor_utils",
        "//src/libraries/metrics",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_recipes",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
