load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "enums",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.enums",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account_identifier",
        "//third_party:com_google_guava_guava",
        "//third_party:org_apache_commons_commons_lang3",
    ],
)
