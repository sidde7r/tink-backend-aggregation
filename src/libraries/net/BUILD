load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "net",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/log:log",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        # todo, don't have dependency on dropwizard
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:tink_org_apache_httpcomponents_httpclient",
        "//third_party:org_json_json",
    ],
)
