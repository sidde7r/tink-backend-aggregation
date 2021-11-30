load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "bouncycastle",
    exports = [
        "@agent_sdk_maven//:org_bouncycastle_bcpkix_jdk15on",
        "@agent_sdk_maven//:org_bouncycastle_bcprov_jdk15on",
    ],
)

java_library(
    name = "serialization_utils",
    srcs = ["@tink_backend//src/libraries/serialization_utils:serialization_utils_srcjar"],
    visibility = ["//src/agent_sdk:__subpackages__"],
    deps = [
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_core",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_databind",
        "@agent_sdk_maven//:com_fasterxml_jackson_dataformat_jackson_dataformat_smile",
        "@agent_sdk_maven//:com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
        "@agent_sdk_maven//:com_google_guava_guava",
        "@agent_sdk_maven//:commons_codec_commons_codec",
        "@agent_sdk_maven//:org_apache_commons_commons_lang3",
        "@agent_sdk_maven//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "account_identifier",
    srcs = ["@tink_backend//src/libraries/account_identifier:account_identifier_srcjar"],
    visibility = ["//src/agent_sdk:__subpackages__"],
    deps = [
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_core",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_databind",
        "@agent_sdk_maven//:com_google_code_findbugs_jsr305",
        "@agent_sdk_maven//:com_google_guava_guava",
        "@agent_sdk_maven//:commons_validator_commons_validator",
        "@agent_sdk_maven//:org_apache_commons_commons_lang3",
        "@agent_sdk_maven//:org_apache_httpcomponents_httpclient",
        "@agent_sdk_maven//:org_apache_httpcomponents_httpcore",
        "@agent_sdk_maven//:org_iban4j_iban4j",
        "@agent_sdk_maven//:org_slf4j_slf4j_api",
    ],
)
