load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "uuid",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.uuid",
    visibility = ["//visibility:public"],
    deps = [
        # UUID
        "//third_party:com_datastax_cassandra_cassandra_driver_core",

        # Serialization
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
    ],
)

junit_test(
    name = "uuid_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/uuid",
        "//third_party:org_assertj_assertj_core",
    ],
)
