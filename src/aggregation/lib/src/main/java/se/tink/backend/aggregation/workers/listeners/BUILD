load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "listeners",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/context",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents",
        "//src/integration/signableoperation:enums",
        "//src/integration/signableoperation:rpc",
        "//third_party:com_google_guava_guava",
    ],
)
