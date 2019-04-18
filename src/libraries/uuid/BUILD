load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "uuid",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.uuid",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",

        # com.datastax.driver.core.utils.UUID
        "@maven//:com_datastax_cassandra_cassandra_driver_core",
    ],
)

junit_test(
    name = "uuid_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":uuid",
        "@maven//:org_assertj_assertj_core",
    ],
)
