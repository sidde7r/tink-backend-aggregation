load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "rpc",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base/entities",
        "//src/integration/lib:annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
