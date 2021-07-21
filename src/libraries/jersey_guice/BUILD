load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "jersey_guice",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        # Guava
        "@aggregation//:com_google_guava_guava",

        # javax.annotation
        "//third_party:com_google_code_findbugs_jsr305",

        # Guice
        "//third_party:com_google_inject_guice",
        "//third_party:com_google_inject_extensions_guice_multibindings",

        # Dropwizard
        "//third_party:io_dropwizard_dropwizard_jersey",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_apache_httpcomponents_httpclient",

        # Javax inject
        "@aggregation//:javax_inject_javax_inject",
        "@aggregation//:javax_annotation_javax_annotation_api",
    ],
)

junit_test(
    name = "jersey_guice_test",
    srcs = glob(["src/test/**/*.java"]),
    runtime_deps = [
        "//third_party:asm_asm",
    ],
    deps = [
        "//src/libraries/jersey_guice",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_jersey",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_netflix_governator_governator",
    ],
)
