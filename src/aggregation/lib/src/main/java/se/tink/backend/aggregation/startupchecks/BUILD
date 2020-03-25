load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "startupchecks",
    srcs = glob(["*.java"]),
    visibility = ["//src/aggregation/lib:__subpackages__"],
    deps = [
        "//src/integration/tpp_secrets_service/client",
        "//src/integration/tpp_secrets_service/client/src/main/java/se/tink/backend/integration/tpp_secrets_service/client/iface",
        "//src/libraries/health_check_handler",
        "//src/libraries/http:http_utils",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:prometheus",
        "@aggregation//:com_google_guava_guava",
    ],
)
