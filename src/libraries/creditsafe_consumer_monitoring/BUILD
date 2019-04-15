load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "creditsafe_consumer_monitoring",
    srcs = glob(["src/main/java/**/*.java"]),
    groupId = "se.tink.libraries.creditsafe.consumermonitoring",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_fasterxml_jackson_dataformat_jackson_dataformat_xml",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:com_google_guava_guava",
        "//third_party:commons_io_commons_io",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
