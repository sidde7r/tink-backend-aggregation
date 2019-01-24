load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "account-identifier",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/date",
        "//src/libraries/log",
        "//src/libraries/uuid",
        "//src/libraries/giro_validation:giro-validation",

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
    name = "account-identifier-test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        ":account-identifier",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
