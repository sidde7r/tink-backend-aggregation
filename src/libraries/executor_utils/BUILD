load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "executor_utils",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.executor",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/concurrency",
        "@maven//:com_google_guava_guava",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
