load("//tools/bzl:junit.bzl", "junit_test")

data = [
    "//data:agents",
    "//src/aggregation/fake_aggregation_controller/src/main/java/se/tink/backend/fake_aggregation_controller:image.tar",
    "//src/aggregation/service:aggregation_decoupled_image.tar",
]

tags = [
    # These tests are not fully hermetic as they depend on Docker. Also a bit slow. Ergo, manual.
    "external",
    "manual",
]

runtime_deps = [
    "//third_party:org_apache_httpcomponents_httpclient",
]

deps = [
    "//src/aggregation/service/src/test/java/se/tink/backend/aggregation/service/utils",
    "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
    "//third_party:lombok_plugin",
    "//third_party:org_assertj_assertj_core",
    "@aggregation//:org_projectlombok_lombok",
    "@system_tests//:com_fasterxml_jackson_core_jackson_databind",
    "@system_tests//:com_github_docker_java_docker_java_api",
    "@system_tests//:com_google_guava_guava",
    "@system_tests//:commons_io_commons_io",
    "@system_tests//:org_apache_httpcomponents_httpclient",
    "@system_tests//:org_assertj_assertj_core",
    "@system_tests//:org_hamcrest_hamcrest_core",
    "@system_tests//:org_hamcrest_hamcrest_library",
    "@system_tests//:org_slf4j_slf4j_api",
    "@system_tests//:org_springframework_boot_spring_boot_test",
    "@system_tests//:org_springframework_spring_test",
    "@system_tests//:org_springframework_spring_web",
    "@system_tests//:org_testcontainers_testcontainers",
]

junit_test(
    name = "service",
    srcs = glob(["*.java"]),
    classpath_resources = ["//src/aggregation/service/src/test/java/se/tink/backend/aggregation/service/resources:local"],
    data = data,
    tags = tags,
    visibility = ["//visibility:public"],
    runtime_deps = runtime_deps,
    deps = deps,
)

junit_test(
    name = "ci",
    srcs = glob(["*.java"]),
    classpath_resources = ["//src/aggregation/service/src/test/java/se/tink/backend/aggregation/service/resources:ci"],
    data = data,
    tags = tags,
    visibility = ["//visibility:public"],
    runtime_deps = runtime_deps,
    deps = deps,
)
