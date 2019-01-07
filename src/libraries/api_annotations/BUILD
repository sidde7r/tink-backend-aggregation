load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "api-annotations",
    srcs = glob(["src/main/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
)


junit_test(
    name = "api-annotations-testlib-test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/api_annotations:api-annotations",
        "//src/libraries/log:log",
        "//third_party:com_google_guava_guava",
        "//third_party:org_javassist_javassist",
        "//third_party:org_reflections_reflections",
        "//third_party:junit_junit",
        "//third_party:com_sun_jersey_jersey_client",
    ],
)
