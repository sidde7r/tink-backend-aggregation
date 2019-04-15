load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "net",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.net",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        # todo, don't have dependency on dropwizard
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_json_json",
    ],
)
