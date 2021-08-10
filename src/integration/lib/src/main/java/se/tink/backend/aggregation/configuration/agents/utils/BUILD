load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "utils",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib:annotations",
        "//third_party:com_google_code_gson_gson",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_json_json",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
    ],
)
