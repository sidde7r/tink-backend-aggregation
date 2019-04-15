load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "dropwizard_utils",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.dropwizard",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/uuid",
        "//third_party:com_netflix_governator",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_protostuff_protostuff_api",
        "//third_party:io_protostuff_protostuff_core",
        "//third_party:io_protostuff_protostuff_runtime",
        "//third_party:io_swagger_swagger_jaxrs",
        "//third_party:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "dropwizard_utils_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/dropwizard_utils",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:joda_time_joda_time",
        "//third_party:junit_junit",
        "//third_party:org_yaml_snakeyaml",
    ],
)
