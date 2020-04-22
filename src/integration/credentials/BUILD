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
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller:__subpackages__",
        "//src/integration:__subpackages__",
    ],
    deps = [
        "//src/integration/credentials:enums",
        "//src/libraries/field:rpc",
        "//src/libraries/serialization",
        "//src/libraries/uuid",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_google_guava_guava",
    ],
)

alias(
    name = "service",
    actual = "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/integration:__subpackages__",
    ],
)
