load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "openid_test",
    srcs = glob(["**/*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/crypto",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/encoding",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas/proxy",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner/identity",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/openid",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/openid/jwt/signer",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/openid/jwt/signer/iface",
        "//src/libraries/serialization",
        "//third_party:com_nimbusds_nimbus_jose_jwt",
        "@aggregation//:com_google_guava_guava",
    ],
)
