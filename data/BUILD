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
    name = "common-lib-test",
    srcs = glob([
        "test/*",
        "templates/*",
    ]),
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
    name = "tink-ca",
    srcs = ["security/TinkCA.truststore"],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "agents",
    srcs = glob(["agents/**"]),
    visibility = ["//visibility:public"],
)

filegroup(
    name = "main-api-test",
    srcs = glob(["test/*"]) + [
        "seeding/iso_countries.csv",
        "seeding/cities-global.json",
        "seeding/cities-se.txt",
        "seeding/cities-nl.txt",
    ],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "aggregation-test",
    srcs = glob([
        "aggregation/**",
        "demo/**",
        "seeding/providers-*.json",
    ]),
    visibility = ["//visibility:public"],
)

filegroup(
    name = "provider-configuration-test",
    srcs = glob([
        "seeding/providers-*.json",
        "seeding/providers/**",
    ]),
    visibility = ["//visibility:public"],
)

filegroup(
    name = "cryptography-test",
    srcs = [
        "test/cryptography/private_rsa_key.pem",
        "test/cryptography/public_rsa_key.pub",
    ],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "tesseract-training-set",
    srcs = glob(
        include = ["tesseract/**"],
    ),
    visibility = ["//visibility:public"],
)
