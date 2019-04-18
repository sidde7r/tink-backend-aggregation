load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "dropwizard_utils",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.dropwizard",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/uuid",
        "//third_party:io_dropwizard_dropwizard_configuration",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_lifecycle",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_inject_guice",
        "@maven//:com_netflix_governator_governator",
        "@maven//:io_protostuff_protostuff_api",
        "@maven//:io_protostuff_protostuff_core",
        "@maven//:io_protostuff_protostuff_runtime",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "dropwizard_utils_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/dropwizard_utils",
        "//third_party:io_dropwizard_dropwizard_core",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_sun_jersey_jersey_server",
        "@maven//:javax_servlet_javax_servlet_api",
        "@maven//:joda_time_joda_time",
        "@maven//:junit_junit",
        "@maven//:org_yaml_snakeyaml",
    ],
)
