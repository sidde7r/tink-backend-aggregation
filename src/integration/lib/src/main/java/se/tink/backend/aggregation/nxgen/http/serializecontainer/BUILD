load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "serializecontainer",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filters/persistent",
        "//third_party:org_apache_httpcomponents_httpclient",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
    ],
)
