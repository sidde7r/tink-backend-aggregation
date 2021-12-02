load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "transaction",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/amount",
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/card",
        "//src/integration/models",
        "//src/integration/transfer:rpc",
        "//src/libraries/date",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_google_guava_guava",
        "//third_party:lombok_libs",
        "@tink_backend//src/libraries/serialization_utils",
        "@tink_backend//src/libraries/uuid",
    ],
)
