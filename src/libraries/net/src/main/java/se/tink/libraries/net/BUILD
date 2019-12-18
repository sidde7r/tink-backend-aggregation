java_library(
    name = "net",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/net/src/main/java/se/tink/libraries/net/client",
        "//src/libraries/net/src/main/java/se/tink/libraries/net/client/handler",
        "//src/libraries/net/src/main/java/se/tink/libraries/net/client/factory",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:com_sun_jersey_jersey_client",
        # todo, don't have dependency on dropwizard
        "//third_party:io_dropwizard_dropwizard_jersey",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_json_json",
    ],
)
