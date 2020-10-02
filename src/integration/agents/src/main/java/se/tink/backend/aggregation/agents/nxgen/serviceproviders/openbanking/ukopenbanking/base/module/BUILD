java_library(
    name = "module",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/base",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/crypto",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/encoding",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/openid",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/openid/jwt/signer",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/openid/jwt/signer/fake",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/openid/jwt/signer/iface",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/libraries/serialization",
        "//third_party:com_google_inject_guice",
    ],
)
