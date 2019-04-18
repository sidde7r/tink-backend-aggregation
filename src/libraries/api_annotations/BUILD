load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "api_annotations",
    srcs = glob(["src/main/**/*.java"]),
    artifactId = "apiannotations",
    groupId = "se.tink.libraries.api",
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
)

junit_test(
    name = "api_annotations_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/api_annotations",
        "@maven//:com_google_guava_guava",
        "@maven//:com_sun_jersey_jersey_client",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:junit_junit",
        "@maven//:org_javassist_javassist",
        "@maven//:org_reflections_reflections",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
