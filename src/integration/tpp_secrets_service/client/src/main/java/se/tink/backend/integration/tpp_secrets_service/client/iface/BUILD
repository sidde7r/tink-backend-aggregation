load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "iface",
    srcs = glob(
        ["*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/tpp_secrets_service/client/src/main/java/se/tink/backend/integration/tpp_secrets_service/client/entities",
    ],
)
