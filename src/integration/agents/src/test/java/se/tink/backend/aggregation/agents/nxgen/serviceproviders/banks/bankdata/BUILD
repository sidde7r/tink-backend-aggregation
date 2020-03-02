load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bankdata",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bankdata",
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//third_party:commons_codec_commons_codec",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
