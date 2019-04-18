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
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_google_guava_guava",
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
        "//third_party:io_dropwizard_dropwizard_configuration",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:javax_validation_validation_api",
        "@maven//:net_bytebuddy_byte_buddy",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_mockito_mockito_core",
        "@maven//:org_objenesis_objenesis",
    ],
)
