load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "tools",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    exports = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/tools/jwt",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/tools/jwt/kid",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/tools/jwt/signer",
    ],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents",
        "//src/libraries/strings",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_reflections_reflections",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_kjetland_mbknor_jackson_jsonschema_2_12",
        "@aggregation//:org_yaml_snakeyaml",
    ],
)
