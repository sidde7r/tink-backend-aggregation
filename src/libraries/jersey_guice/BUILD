load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "jersey-guice",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        # Guava
        "//third_party:com_google_guava_guava",

        # Guice
        "//third_party:com_google_inject_guice",
        "//third_party:com_google_inject_extensions_guice_multibindings",

        # Dropwizard
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_apache_httpcomponents_httpclient",
    ],
)

junit_test(
    name = "jersey-guice-test",
    srcs = glob(["src/test/**/*.java"]),
    runtime_deps = [
        "//third_party:asm_asm",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        ":jersey-guice",

        # Governator
        "//third_party:com_netflix_governator",

        # Mockito
        "//third_party:org_mockito_mockito_core",

        # Jersey
        "//third_party:io_dropwizard_dropwizard_jersey",
    ],
)
