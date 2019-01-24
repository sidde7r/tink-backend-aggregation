load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "cryptography",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_auth0_java_jwt",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:commons_codec_commons_codec",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)

junit_test(
    name = "cryptography-test",
    srcs = glob(["src/test/**/*.java"]),
    data = [
        "//data:cryptography-test",
    ],
    deps = [
        ":cryptography",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)
