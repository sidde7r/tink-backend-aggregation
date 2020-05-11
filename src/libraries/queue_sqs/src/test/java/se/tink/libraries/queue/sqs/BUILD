load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

junit_test(
    name = "sqs",
    srcs = glob(["*.java"]),
    visibility = ["//src/libraries/queue_sqs:__subpackages__"],
    deps = [
        "//src/libraries/queue_sqs",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
