java_library(
    name = "utils",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:org_apache_httpcomponents_httpclient",
    ],
    deps = [
        "//third_party:lombok_plugin",
        "@aggregation//:org_projectlombok_lombok",
        "@org_slf4j_slf4j_api",
        "@system_tests//:com_fasterxml_jackson_core_jackson_databind",
        "@system_tests//:com_google_guava_guava",
        "@system_tests//:commons_io_commons_io",
        "@system_tests//:org_apache_httpcomponents_httpclient",
        "@system_tests//:org_hamcrest_hamcrest_core",
        "@system_tests//:org_hamcrest_hamcrest_library",
        "@system_tests//:org_springframework_boot_spring_boot_test",
        "@system_tests//:org_springframework_spring_test",
        "@system_tests//:org_springframework_spring_web",
        "@system_tests//:org_testcontainers_testcontainers",
    ],
)
