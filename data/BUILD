filegroup(
    name = "data",
    srcs = glob(
        include = ["**"],
        exclude = [
            "BUILD",
            "test/**",
        ],
    ),
    visibility = ["//visibility:public"],
)

filegroup(
    name = "version",
    srcs = ["version.json"],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "security",
    srcs = glob(["security/**"]),
    visibility = ["//visibility:public"],
)

filegroup(
    name = "provision_clients",
    srcs = glob(["provision/clients/**"]),
    visibility = ["//visibility:public"],
)

filegroup(
    name = "tink_ca",
    srcs = ["security/TinkCA.truststore"],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "agents",
    srcs = glob(["agents/**"]),
    visibility = ["//visibility:public"],
)

filegroup(
    name = "aggregation_test",
    srcs = glob([
        "aggregation/**",
        "demo/**",
        "eidas_dev_certificates/**",
        "seeding/providers-*.json",
    ]),
    visibility = ["//visibility:public"],
)

filegroup(
    name = "cryptography_test",
    srcs = [
        "test/cryptography/private_rsa_key.pem",
        "test/cryptography/public_rsa_key.pub",
    ],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "agent_unit_tests",
    srcs = glob([
        "test/agents/**",
    ]),
    visibility = ["//visibility:public"],
)

filegroup(
    name = "tesseract_training_set",
    srcs = glob(
        include = ["tesseract/**"],
    ),
    visibility = ["//visibility:public"],
)
