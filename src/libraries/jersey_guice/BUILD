load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "jersey_guice",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.jersey.guice",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_jersey",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_extensions_guice_multibindings",
        "@maven//:com_google_inject_guice",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_apache_httpcomponents_httpclient",
        "@maven//:org_apache_httpcomponents_httpcore",
    ],
)

junit_test(
    name = "jersey_guice_test",
    srcs = glob(["src/test/**/*.java"]),
    runtime_deps = [
        "@maven//:asm_asm",
        "@maven//:net_bytebuddy_byte_buddy",
        "@maven//:org_objenesis_objenesis",
    ],
    deps = [
        ":jersey_guice",
        "//third_party:io_dropwizard_dropwizard_jersey",
        "@maven//:com_google_inject_guice",
        "@maven//:com_netflix_governator_governator",
        "@maven//:org_mockito_mockito_core",
    ],
)
