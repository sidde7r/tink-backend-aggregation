load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "creditsafe_consumer_monitoring",
    srcs = glob(["src/main/java/**/*.java"]),
    groupId = "se.tink.libraries.creditsafe.consumermonitoring",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_fasterxml_jackson_dataformat_jackson_dataformat_xml",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:commons_io_commons_io",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
