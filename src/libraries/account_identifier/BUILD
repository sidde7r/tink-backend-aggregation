load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "account_identifier",
    srcs = glob(["src/main/**/*.java"]),
    artifactId = "account-identifier",
    groupId = "se.tink.libraries.account",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_slf4j_slf4j_api",
        "//src/libraries/uuid",
        "//src/libraries/giro_validation",
        "//src/libraries/strings",

        # Guava
        "//third_party:com_google_guava_guava",

        # Jackson
        "//third_party:com_fasterxml_jackson_core_jackson_databind",

        # IBAN
        "//third_party:org_iban4j_iban4j",

        # HTTP
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",

        # Stringutils
        "//third_party:org_apache_commons_commons_lang3",
    ],
)

junit_test(
    name = "account_identifier_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account_identifier",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
