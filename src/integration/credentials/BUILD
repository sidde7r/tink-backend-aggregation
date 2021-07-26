load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "demo",
    srcs = glob(["src/main/**/demo/*.java"]),
    visibility = [
        "//src/integration:__subpackages__",
        "//src/libraries/social_security_number:__pkg__",
    ],
    deps = [
        "@aggregation//:com_google_guava_guava",
    ],
)

junit_test(
    name = "demo_test",
    srcs = glob(["src/test/**/demo/*.java"]),
    visibility = ["//src/integration:__subpackages__"],
    deps = [
        "//src/integration/credentials:demo",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)

junit_test(
    name = "service_test",
    srcs = glob(["src/test/**/service/*.java"]),
    visibility = ["//src/integration:__subpackages__"],
    deps = [
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@tink_backend//src/libraries/account_identifier",
    ],
)

java_library(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    visibility = ["//src/integration:__subpackages__"],
)

alias(
    name = "service",
    actual = "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/integration:__subpackages__",
    ],
)
