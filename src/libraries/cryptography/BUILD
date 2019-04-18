load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "cryptography",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.cryptography",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_auth0_java_jwt",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:commons_codec_commons_codec",
        "@maven//:org_bouncycastle_bcpkix_jdk15on",
        "@maven//:org_bouncycastle_bcprov_jdk15on",
        "@maven//:org_slf4j_slf4j_api",
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
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_bouncycastle_bcpkix_jdk15on",
        "@maven//:org_bouncycastle_bcprov_jdk15on",
    ],
)
