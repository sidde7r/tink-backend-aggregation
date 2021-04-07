load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "amount",
    srcs = glob(["src/main/**/*.java"]),
    visibility = [
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller:__subpackages__",
        "//src/aggregation/aggregationcontroller_api/src/test/java/se/tink/backend/aggregation/aggregationcontroller/fake:__subpackages__",
        "//src/aggregation/api:__subpackages__",
        "//src/integration:__subpackages__",
    ],
    deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
        "@aggregation//:com_google_guava_guava",
    ],
)

junit_test(
    name = "amount-test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//src/integration:__subpackages__"],
    deps = [
        ":amount",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
    ],
)
