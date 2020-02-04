load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "banks",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/amount",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//src/libraries/account_identifier",
        "//third_party:com_google_guava_guava",
        "//third_party:org_hamcrest_hamcrest_library",
    ],
)
