load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "service_version",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.service.version",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/metrics",
        "//src/libraries/serialization",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_guice",
    ],
)
