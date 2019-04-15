load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "cryptography",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.cryptography",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_auth0_java_jwt",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:commons_codec_commons_codec",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "cryptography_test",
    srcs = glob(["src/test/**/*.java"]),
    data = [
        "//data:cryptography_test",
    ],
    deps = [
        "//src/libraries/cryptography",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)
