load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "account",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:com_google_guava_guava",
        "//third_party:io_vavr",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
    ],
    intdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/source_info",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/card",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_capabilities",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/account/src/main/java/se/tink/libraries/account/enums",
        "//src/integration/amount",
        "//src/integration/models",
        "//src/libraries/agent-storage",
        "//src/libraries/date",
        "//src/libraries/pair",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/accountholder",
        "@tink_backend//src/libraries/account_identifier",
        "@tink_backend//src/libraries/strings",
    ],
    visibility = ["//visibility:public"],
)
