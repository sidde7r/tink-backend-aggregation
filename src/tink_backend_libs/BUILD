load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "aggregation_agent_api_client",
    srcs = ["@tink_backend//src/libraries/aggregation_agent_api_client:aggregation_agent_api_client_srcjar"],
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
        "//third_party:com_google_guava_guava",
        "//third_party:javax_validation_validation_api",
        "//third_party:lombok_plugin",
        "//third_party:org_hibernate_hibernate_validator",
        "@aggregation//:org_projectlombok_lombok",
        "@maven//:com_squareup_okhttp3_okhttp",
        "@maven//:com_squareup_okhttp3_okhttp_tls",
        "@maven//:com_squareup_okio_okio",
        "@maven//:javax_el_javax_el_api",
    ],
)
