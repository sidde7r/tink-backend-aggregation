load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "api",
    srcs = glob(["src/main/**/*.java"]),
    artifactId = "integration-api",
    groupId = "se.tink.backend.agents",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account:enums",
        "//src/libraries/account_identifier",
        "//src/libraries/credentials:demo",
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_google_guava_guava",
    ],
)

junit_test(
    name = "api_test",
    srcs = glob(["src/test/**/*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api",
        "//src/libraries/serialization",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:io_dropwizard_dropwizard_configuration",
        "//third_party:javax_validation_validation_api",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_objenesis_objenesis",
    ],
)
