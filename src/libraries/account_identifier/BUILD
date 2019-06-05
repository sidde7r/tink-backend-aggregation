load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "account_identifier",
    srcs = glob(["src/main/**/*.java"]),
    artifactId = "account-identifier",
    groupId = "se.tink.libraries.account",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/giro_validation",
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_apache_httpcomponents_httpclient",
        "@maven//:org_apache_httpcomponents_httpcore",
        "@maven//:org_iban4j_iban4j",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "account_identifier_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account_identifier",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)
