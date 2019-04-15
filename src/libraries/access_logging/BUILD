load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "access_logging",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.accesslogging",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/auth",
        "//third_party:com_google_guava_guava",
        "//third_party:org_slf4j_jul_to_slf4j",
    ],
)
