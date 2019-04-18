load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "auth",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.auth",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/http:http_annotations",
        "//third_party:io_dropwizard_dropwizard_core",
        "@maven//:com_google_guava_guava",
        "@maven//:com_lambdaworks_scrypt",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:com_sun_jersey_jersey_server",
        "@maven//:com_yubico_yubico_validation_client2",
        "@maven//:javax_servlet_javax_servlet_api",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:org_springframework_security_spring_security_core",
    ],
)

junit_test(
    name = "auth_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/auth",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)
