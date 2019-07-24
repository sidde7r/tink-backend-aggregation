load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "queue_lib",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.queue",
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_google_guava_guava",
    ],
)
