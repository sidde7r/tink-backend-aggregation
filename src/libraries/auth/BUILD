load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "auth",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/http:http_annotations",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:com_google_guava_guava",
        "//third_party:com_lambdaworks_scrypt",
        "//third_party:com_yubico_yubico_validation_client2",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_springframework_security",
    ],
)

junit_test(
    name = "auth_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/auth",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
