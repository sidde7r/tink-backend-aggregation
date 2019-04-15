load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "executor_utils",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.executor",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/concurrency",
        "//third_party:com_google_guava_guava",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
