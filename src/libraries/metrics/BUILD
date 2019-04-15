load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "metrics",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.metrics",
    visibility = ["//visibility:public"],
    deps = [
        # Guava
        "//third_party:com_google_guava_guava",

        # Guice
        "//third_party:com_google_inject_guice",

        # Prometheus
        "//third_party:io_prometheus_simpleclient",
        "//third_party:io_prometheus_simpleclient_hotspot",
        "//third_party:io_prometheus_simpleclient_servlet",

        # Slf4j
        "//third_party:org_slf4j_slf4j_api",

        # JSR 305
        "//third_party:com_google_code_findbugs_jsr305",

        # Logback
        "//third_party:ch_qos_logback_logback_classic",

        # Javax
        "//third_party:org_eclipse_jetty_orbit_javax_servlet",

        # Dropwizard (needed for the Jetty HTTP server)
        "//third_party:io_dropwizard_dropwizard_core",

        # Spring (needed for StopWatch)
        "//third_party:org_springframework_data_spring_data_commons",
    ],
)

junit_test(
    name = "metrics_test",
    srcs = glob(["src/test/**/*.java"]),
    runtime_deps = [
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        "//src/libraries/metrics",
        "//third_party:com_google_guava_guava",
        "//third_party:io_prometheus_simpleclient",
        "//third_party:io_prometheus_simpleclient_common",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
