load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "discovered_web_service",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/discovery/src/main/java/se/tink/libraries/discovery",
        "//src/libraries/http_client",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
    ],
)
