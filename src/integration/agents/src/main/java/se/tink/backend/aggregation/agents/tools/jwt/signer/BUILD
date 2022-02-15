load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "signer",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base/configuration",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/common/openid/jwt/signer/iface",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/tools/jwt",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/tools/jwt/kid",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents/utils",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidasidentity/identity",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner",
        "//src/libraries/serialization",
        "//third_party:com_nimbusds_nimbus_jose_jwt",
        "//third_party:lombok_libs",
        "//third_party:net_minidev_json_smart",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
    ],
)
