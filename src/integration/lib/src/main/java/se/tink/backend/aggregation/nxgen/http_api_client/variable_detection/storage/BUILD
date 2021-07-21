load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "storage",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/authentication",
        "//src/libraries/serialization",
        "//src/tink_backend_libs:aggregation_agent_api_client",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
    ],
)
