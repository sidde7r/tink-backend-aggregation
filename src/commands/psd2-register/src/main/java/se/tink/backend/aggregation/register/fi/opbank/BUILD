java_binary(
    name = "opbank",
    srcs = glob(["**/*.java"]),
    main_class = "se.tink.backend.aggregation.register.fi.opbank.OPBankRegisterCommand",
    visibility = ["//visibility:public"],
    deps = [
        "//src/commands/psd2-register/src/main/java/se/tink/backend/aggregation/register:psd2-register",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidas",
        "//src/libraries/serialization",
        "//src/libraries/uuid",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_code_gson_gson",
        "//third_party:com_nimbusds_nimbus_jose_jwt",
        "//third_party:com_sun_jersey_jersey_core",
    ],
)
