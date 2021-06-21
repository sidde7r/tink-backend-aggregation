load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "storage",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/authentication",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http_api_client/variable_detection/storage",
        "//src/tink_backend_libs:aggregation_agent_api_client",
        "//third_party:junit_junit",
    ],
)
