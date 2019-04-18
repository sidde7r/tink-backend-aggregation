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
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",

        # com.datastax.driver.core.utils.UUID
        "@maven//:com_datastax_cassandra_cassandra_driver_core",
    ],
)
