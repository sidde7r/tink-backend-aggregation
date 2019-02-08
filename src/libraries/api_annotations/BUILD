load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "api_annotations",
    srcs = glob(["src/main/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
)

junit_test(
    name = "api_annotations_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/api_annotations",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:junit_junit",
        "//third_party:org_javassist_javassist",
        "//third_party:org_reflections_reflections",
    ],
)
