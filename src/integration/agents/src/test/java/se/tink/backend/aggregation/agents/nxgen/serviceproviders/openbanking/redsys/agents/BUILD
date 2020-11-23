load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "es-redsys-santander-ob",
    srcs = glob(["*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)
