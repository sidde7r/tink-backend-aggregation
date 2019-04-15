load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "jersey_guice",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.jersey.guice",
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
    name = "jersey_guice_test",
    srcs = glob(["src/test/**/*.java"]),
    runtime_deps = [
        "//third_party:asm_asm",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        "//src/libraries/jersey_guice",
        "//third_party:com_netflix_governator",
        "//third_party:io_dropwizard_dropwizard_jersey",
        "//third_party:org_mockito_mockito_core",
    ],
)
