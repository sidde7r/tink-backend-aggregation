load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "storage",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base/entities",
        "//src/integration/lib",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
    ],
)
