load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "access_logging",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.accesslogging",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/auth",
        "@maven//:com_google_guava_guava",
        "@maven//:org_slf4j_jul_to_slf4j",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
