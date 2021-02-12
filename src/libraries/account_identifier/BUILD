load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "account_identifier",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@aggregation//:commons_validator_commons_validator",
        "//third_party:org_slf4j_slf4j_api",
        "//src/libraries/uuid",
        "//src/libraries/giro_validation",
        "//src/libraries/strings",
        "//src/libraries/social_security_number",

        # Guava
        "@aggregation//:com_google_guava_guava",

        # Jackson
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",

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
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
