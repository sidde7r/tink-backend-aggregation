java_library(
    name = "signer",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/crypto/ps256",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/openid/jwt/signer/iface",
        "//third_party:com_nimbusds_nimbus_jose_jwt",
        "//third_party:net_minidev_json_smart",
    ],
)
