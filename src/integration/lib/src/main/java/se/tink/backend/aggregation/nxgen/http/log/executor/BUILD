load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "executor",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/log/constants",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/utils",
        "//src/libraries/date",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
