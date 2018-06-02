load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "consent-lib",
    srcs = glob(["src/main/**/*.java"]),
    deps = [
        "//src/libraries/cassandra-interfaces",
        "//src/libraries/versioning:versioning",
        "//src/libraries/uuid:uuid",
        "//src/libraries/cryptography",
        "//src/libraries/serialization_utils:serialization-utils",
                
        "//third_party:com_fasterxml_jackson_core_jackson_core",        
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:io_protostuff_protostuff_api",
        "//third_party:io_swagger_swagger_annotations",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_modelmapper_modelmapper",
        "//third_party:org_springframework_data_spring_data_cassandra",
        "//third_party:org_xerial_snappy_snappy_java",
        "//third_party:org_apache_commons_commons_lang3",
    ],
    visibility = ["//visibility:public"],
)

junit_test(
    name = "consent-lib-test",
    srcs = glob(["src/test/**/*.java"]),
    runtime_deps = [
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        ":consent-lib",
        "//src/libraries/uuid:uuid",
        "//src/libraries/cryptography",
        
        "//third_party:com_google_guava_guava",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
