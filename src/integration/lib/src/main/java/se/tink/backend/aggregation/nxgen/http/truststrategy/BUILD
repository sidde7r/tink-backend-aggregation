load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "truststrategy",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/crypto/parser",
        "//third_party:org_apache_httpcomponents_httpclient",
    ],
)
