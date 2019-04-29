load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bawagpsk",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:provider_configuration_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/at/banks/easybank/bawagpsk",
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//third_party:com_google_guava_guava",
    ],
)
