load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "demo",
    srcs = glob(["src/main/**/demo/*.java"]),
    visibility = [
        "//src/integration:__subpackages__",
        "//src/libraries/social_security_number:__pkg__",
    ],
    deps = [
        "//third_party:com_google_guava_guava",
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
        "//src/integration/credentials:service",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)

java_library(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    visibility = ["//src/integration:__subpackages__"],
)

java_library(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/integration:__subpackages__",
    ],
    deps = [
        "//src/integration/credentials:enums",
        "//src/libraries/field:rpc",
        "//src/libraries/serialization",
        "//src/libraries/uuid",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_google_guava_guava",
    ],
)

java_library(
    name = "service",
    srcs = glob(["src/main/**/service/*.java"]),
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/integration:__subpackages__",
    ],
    deps = [
        "//src/integration/api",
        "//src/libraries/user:rpc",
        "//src/libraries/uuid",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:commons_codec_commons_codec",
        "@tink_backend//src/libraries/strings",
    ],
)
