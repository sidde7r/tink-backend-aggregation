java_library(
    name = "tools",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/api",
        "//src/integration/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents",
        "//src/libraries/strings",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_code_gson_gson",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_reflections_reflections",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:org_yaml_snakeyaml",
        "@aggregation//:com_google_guava_guava",
    ],
)
