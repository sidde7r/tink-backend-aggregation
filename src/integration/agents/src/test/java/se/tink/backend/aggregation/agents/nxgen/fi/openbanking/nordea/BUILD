load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "nordea",
    srcs = glob(["*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/nordea",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/nordeabase",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)
