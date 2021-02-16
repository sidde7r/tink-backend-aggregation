load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "loader_test",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/module/annotation",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/module/loader",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/module/loader/testclasses",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:org_reflections_reflections",
    ],
)
