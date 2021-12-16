load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "bouncycastle",
    exports = [
        "@aggregation//:org_bouncycastle_bcpkix_jdk15on",
        "@aggregation//:org_bouncycastle_bcprov_jdk15on",
    ],
)

java_library(
    name = "serialization_utils",
    srcs = ["@tink_backend//src/libraries/serialization_utils:serialization_utils_srcjar"],
    visibility = ["//src/agent_sdk:__subpackages__"],
    deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_smile",
        "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:commons_codec_commons_codec",
        "@aggregation//:org_apache_commons_commons_lang3",
        "@aggregation//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "account_identifier",
    srcs = ["@tink_backend//src/libraries/account_identifier:account_identifier_srcjar"],
    visibility = ["//src/agent_sdk:__subpackages__"],
    deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:commons_validator_commons_validator",
        "@aggregation//:org_apache_commons_commons_lang3",
        "@aggregation//:org_apache_httpcomponents_httpclient",
        "@aggregation//:org_apache_httpcomponents_httpcore",
        "@aggregation//:org_iban4j_iban4j",
        "@aggregation//:org_slf4j_slf4j_api",
    ],
)
