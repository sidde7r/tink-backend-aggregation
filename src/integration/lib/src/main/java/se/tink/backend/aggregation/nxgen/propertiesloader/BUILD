load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "propertiesloader",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
        "//third_party:lombok_libs",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_yaml",
        "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
    ],
)

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-minion-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
