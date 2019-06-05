load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "api",
    srcs = glob(["src/main/**/*.java"]),
    artifactId = "aggregation-api",
    groupId = "se.tink.backend.aggregation",
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api",
        "//src/libraries/api_annotations",
        "//src/libraries/credentials:service",
        "//src/libraries/creditsafe_consumer_monitoring",
        "//src/libraries/http:http_annotations",
        "//src/libraries/metrics",
        "//src/libraries/signableoperation:rpc",
        "//src/libraries/transfer:rpc",
        "//src/libraries/user:rpc",
        "//third_party:io_dropwizard_dropwizard_core",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:commons_codec_commons_codec",
        "@maven//:javax_validation_validation_api",
        "@maven//:joda_time_joda_time",
        "@maven//:org_hibernate_hibernate_annotations",
        "@maven//:org_mockito_mockito_core",
    ],
)

junit_test(
    name = "api_test",
    srcs = glob(["src/test/**/*.java"]),
    data = ["//data:tink_ca"],
    runtime_deps = [
        "//src/aggregation/api",
        "//src/libraries/api_annotations",
        "@maven//:ch_qos_logback_logback_classic",
    ],
    deps = [
        "//src/integration/api",
        "//src/libraries/api_annotations:api_annotations_test",
        "//src/libraries/credentials:service",
        "//src/libraries/user:rpc",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:com_sun_jersey_jersey_client",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_javassist_javassist",
        "@maven//:org_reflections_reflections",
    ],
)
