java_binary(
    name = "rabobank",
    srcs = glob(["**/*.java"]),
    data = [
        "//src/commands/psd2-register/src/main/java/se/tink/backend/aggregation/register/nl/rabobank/resources",
    ],
    main_class = "se.tink.backend.aggregation.register.nl.rabobank.RabobankRegisterCommand",
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//src/integration/lib:annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidas",
        "//src/libraries/cryptography",
        "//src/libraries/serialization",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_apache_commons_commons_cli",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_bouncycastle_bcprov_jdk15on",
    ],
)
