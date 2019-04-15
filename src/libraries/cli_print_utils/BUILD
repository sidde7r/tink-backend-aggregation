load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "cli_print_utils",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.cli.printutils",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_http_client_google_http_client",
    ],
)
