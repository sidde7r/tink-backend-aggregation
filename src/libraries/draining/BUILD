load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "draining",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.draining",
    visibility = ["//visibility:public"],
    deps = [
        "@dropwizard//:io_dropwizard_dropwizard_core",
        "@dropwizard//:io_dropwizard_dropwizard_servlets",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
    ],
)
