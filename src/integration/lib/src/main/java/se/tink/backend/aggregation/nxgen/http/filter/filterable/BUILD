load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "filterable",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/engine",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filters/iface",
        "//third_party:org_apache_commons_commons_collections4",
        "@aggregation//:com_google_guava_guava",
    ],
)
