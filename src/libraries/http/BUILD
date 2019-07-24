load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "http_utils",
    srcs = glob(["src/main/java/se/tink/libraries/http/utils/*.java"]),
    groupId = "se.tink.libraries.http.utils",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:org_mockito_mockito_core",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_library_maven(
    name = "http_annotations",
    srcs = glob(["src/main/java/se/tink/libraries/http/annotations/**/*.java"]),
    groupId = "se.tink.libraries.http.annotations",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_google_guava_guava",
        "@maven//:javax_validation_validation_api",
    ],
)
