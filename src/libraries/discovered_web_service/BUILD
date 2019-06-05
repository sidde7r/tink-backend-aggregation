load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "discovered_web_service",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.discovered",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/discovery",
        "//src/libraries/http_client",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_http_client_google_http_client",
        "@maven//:com_sun_jersey_jersey_client",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:org_apache_curator_curator_framework",
        "@maven//:org_apache_curator_curator_x_discovery",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
