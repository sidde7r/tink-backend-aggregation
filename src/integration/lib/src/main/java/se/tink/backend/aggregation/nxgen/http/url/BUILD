load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "url",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_apache_commons_commons_lang3",
        "@aggregation//:com_google_guava_guava",
    ],
)
