load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "supplementalfields",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib:annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/utils",
        "//src/libraries/i18n_aggregation",
        "//src/libraries/serialization",
        "//third_party:com_google_code_gson_gson",
        "//third_party:com_google_guava_guava",
        "//third_party:javax_validation_validation_api",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
    ],
)
