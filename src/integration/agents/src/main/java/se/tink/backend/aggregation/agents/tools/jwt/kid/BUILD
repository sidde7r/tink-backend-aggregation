load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "kid",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/tools/jwt",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents/utils",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//third_party:com_nimbusds_nimbus_jose_jwt",
        "//third_party:io_vavr",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
