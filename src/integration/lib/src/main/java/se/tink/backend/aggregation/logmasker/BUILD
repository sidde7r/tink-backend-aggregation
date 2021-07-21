load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "logmasker",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/utils/masker",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:io_reactivex_rxjava3_rxjava",
    ],
)
