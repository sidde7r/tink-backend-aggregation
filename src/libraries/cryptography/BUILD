load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "cryptography",
    srcs = glob(
        ["*.java"],
        exclude = [
            "*Test.java",
        ],
    ),
    extdeps = [
        "//third_party:commons_codec_commons_codec",
        "//third_party:net_minidev_json_smart",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/libraries/cryptography/hash",
        "//src/libraries/encoding",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "//src/libraries/cryptography/hash",
    ],
)

junit_test(
    name = "cryptography_test",
    srcs = glob(["*Test.java"]),
    data = [
        "//data:cryptography_test",
    ],
    deps = [
        ":cryptography",
        "//src/libraries/encoding",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)
