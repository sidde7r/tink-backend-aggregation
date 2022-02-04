load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "validators",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/common/openid",
        "//src/integration/lib",
        "//third_party:org_apache_httpcomponents_httpcore",
    ],
)
