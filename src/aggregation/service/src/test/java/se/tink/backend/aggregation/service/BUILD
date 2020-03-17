load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "service",
    srcs = glob(["SystemTest.java"]),
    data = [
        "//src/aggregation/service/src/test/java/se/tink/backend/aggregation/resources",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
    ],
    deps = [
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

java_binary(
    name = "wiremock",
    srcs = glob(["BlackboxWireMockServer.java"]),
    data = [
        "//src/aggregation/service/src/test/java/se/tink/backend/aggregation/resources",
    ],
    main_class = "se.tink.backend.aggregation.service.BlackboxWireMockServer",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "@org_slf4j_slf4j_simple//jar",
    ],
    deps = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/wiremock",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/wiremock/entities",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/wiremock/parsing",
        "//src/libraries/metrics:registry",
        "//src/libraries/pair",
        "//src/libraries/strings",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:io_dropwizard_dropwizard_configuration",
        "//third_party:junit_junit",
        "//third_party:lombok_libs",
        "//third_party:lombok_plugin",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
        "@lombok//:org_projectlombok_lombok",
        "@system_tests//:com_fasterxml_jackson_core_jackson_databind",
        "@system_tests//:com_google_guava_guava",
        "@system_tests//:commons_io_commons_io",
        "@system_tests//:org_apache_httpcomponents_httpclient",
        "@system_tests//:org_hamcrest_hamcrest_core",
        "@system_tests//:org_hamcrest_hamcrest_library",
    ],
)
