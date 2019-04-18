load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "metrics",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.metrics",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:io_dropwizard_dropwizard_core",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:io_prometheus_simpleclient",
        "@maven//:io_prometheus_simpleclient_hotspot",
        "@maven//:io_prometheus_simpleclient_servlet",
        "@maven//:javax_inject_javax_inject",
        "@maven//:javax_servlet_javax_servlet_api",
        "@maven//:org_eclipse_jetty_jetty_server",
        "@maven//:org_eclipse_jetty_jetty_servlet",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:org_springframework_spring_core",
    ],
)

junit_test(
    name = "metrics_test",
    srcs = glob(["src/test/**/*.java"]),
    runtime_deps = [
        "@maven//:net_bytebuddy_byte_buddy",
        "@maven//:org_objenesis_objenesis",
    ],
    deps = [
        "//src/libraries/metrics",
        "@maven//:com_google_guava_guava",
        "@maven//:io_prometheus_simpleclient",
        "@maven//:io_prometheus_simpleclient_common",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_mockito_mockito_core",
    ],
)
