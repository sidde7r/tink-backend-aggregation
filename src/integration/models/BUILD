load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "models",
    srcs = glob(["src/main/**/*.java"]),
    artifactId = "integration-models",
    groupId = "se.tink.backend.aggregation.agents.models",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "//third_party:com_datastax_cassandra_cassandra_driver_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
    ],
)
