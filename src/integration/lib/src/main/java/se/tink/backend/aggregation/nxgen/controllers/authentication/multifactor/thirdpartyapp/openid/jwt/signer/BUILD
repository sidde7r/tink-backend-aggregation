java_library(
    name = "signer",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/crypto",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/encoding",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/openid",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/openid/jwt/signer/iface",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/libraries/serialization",
        "//third_party:com_nimbusds_nimbus_jose_jwt",
        "//third_party:io_vavr",
        "//third_party:lombok_libs",
        "//third_party:net_minidev_json_smart",
    ],
)
