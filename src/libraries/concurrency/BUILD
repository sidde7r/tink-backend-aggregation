load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "concurrency",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.concurrency",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/metrics",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_apache_curator_curator_framework",
        "@maven//:org_slf4j_slf4j_api",
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
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:org_apache_curator_curator_client",
        "@maven//:org_apache_curator_curator_framework",
        "@maven//:org_apache_curator_curator_recipes",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
