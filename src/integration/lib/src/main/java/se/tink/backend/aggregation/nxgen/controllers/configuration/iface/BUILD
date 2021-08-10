load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "iface",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents",
        "@aggregation//:io_reactivex_rxjava3_rxjava",
    ],
)
