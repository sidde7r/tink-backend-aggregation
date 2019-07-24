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
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidas",
        "//src/libraries/cryptography",
        "//src/libraries/serialization",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_google_guava_guava",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:commons_cli_commons_cli",
        "@maven//:org_bouncycastle_bcpkix_jdk15on",
        "@maven//:org_bouncycastle_bcprov_jdk15on",
    ],
)
