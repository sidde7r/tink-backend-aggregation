load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "net",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.net",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
        "@maven//:com_sun_jersey_contribs_jersey_apache_client4",
        "@maven//:com_sun_jersey_jersey_client",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:org_apache_httpcomponents_httpclient",
        "@maven//:org_apache_httpcomponents_httpcore",
        "@maven//:org_json_json",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
