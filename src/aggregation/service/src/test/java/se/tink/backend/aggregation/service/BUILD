load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "service",
    srcs = glob(["*.java"]),
    data = [
        "//data/agents/uk/amex",
        "//data/agents/uk/barclays",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:org_apache_httpcomponents_httpclient",
    ],
    deps = [
        "//src/aggregation/service/src/test/java/se/tink/backend/aggregation/service/utils",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "@system_tests//:com_fasterxml_jackson_core_jackson_databind",
        "@system_tests//:com_google_guava_guava",
        "@system_tests//:commons_io_commons_io",
        "@system_tests//:org_apache_httpcomponents_httpclient",
        "@system_tests//:org_hamcrest_hamcrest_core",
        "@system_tests//:org_hamcrest_hamcrest_library",
        "@system_tests//:org_springframework_boot_spring_boot_test",
        "@system_tests//:org_springframework_spring_test",
        "@system_tests//:org_springframework_spring_web",
    ],
)
