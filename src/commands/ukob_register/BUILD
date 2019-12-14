java_binary(
    name = "ukob_register",
    srcs = glob(["src/main/java/se/tink/backend/aggregation/*.java"]),
    main_class = "se.tink.backend.aggregation.UkobRegisterCommand",
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/base",
        "//src/integration/lib",
        "//src/integration/lib:annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/openid",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/libraries/serialization",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)
