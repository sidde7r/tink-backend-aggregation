load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "categorization-api",
    srcs = glob(["src/main/**/*.java"]),
    deps = [
        "//third_party:com_google_guava_guava",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/http_client:http-client",
    ],
    visibility = ["//visibility:public"],
)

junit_test(
    name = "categorization-api-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":categorization-api",
        
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/http_client:http-client",

        "//third_party:com_google_guava_guava",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:com_github_tomakehurst_wiremock",
    ],
    visibility = ["//visibility:public"],
)

