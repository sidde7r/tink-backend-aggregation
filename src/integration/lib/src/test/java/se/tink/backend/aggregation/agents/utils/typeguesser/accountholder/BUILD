load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "accountholder",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/typeguesser/accountholder",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
