load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "discovery",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.discovery",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/uuid",
        "//third_party:io_dropwizard_dropwizard_lifecycle",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:org_apache_curator_curator_client",
        "@maven//:org_apache_curator_curator_framework",
        "@maven//:org_apache_curator_curator_recipes",
        "@maven//:org_apache_curator_curator_x_discovery",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "discovery_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/discovery",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_apache_curator_curator_x_discovery",
        "@maven//:org_assertj_assertj_core",
        "@maven//:pl_pragmatists_JUnitParams",
    ],
)
