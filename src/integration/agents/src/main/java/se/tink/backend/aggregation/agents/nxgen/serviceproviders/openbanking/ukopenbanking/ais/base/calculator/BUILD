load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "calculator",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base/api",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base/entities",
        "//src/integration/amount",
        "//src/libraries/mapper",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
