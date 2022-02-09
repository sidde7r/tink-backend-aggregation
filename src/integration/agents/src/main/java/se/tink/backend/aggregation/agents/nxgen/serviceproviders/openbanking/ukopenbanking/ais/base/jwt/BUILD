load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "jwt",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//third_party:com_nimbusds_nimbus_jose_jwt",
        "//third_party:lombok_libs",
    ],
)
