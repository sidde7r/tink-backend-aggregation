alias(
    name = "provider_configurations",
    actual = "@tink_backend//src/provider_configuration/data/seeding",
    visibility = ["//visibility:public"],
)

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
    name = "eidas_dev",
    srcs = glob([
        "eidas_dev_certificates/**",
    ]),
    visibility = ["//visibility:public"],
)

filegroup(
    name = "aggregation_test",
    srcs = glob([
        "aggregation/**",
        "demo/**",
        "eidas_dev_certificates/**",
    ]),
    visibility = ["//visibility:public"],
)

filegroup(
    name = "cryptography_all_files",
    srcs = glob([
        "test/cryptography/**/*",
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

filegroup(
    name = "qsealc_crt",
    srcs = [
        "test/qsealc/ca.crt",
        "test/qsealc/proxytls.crt",
        "test/qsealc/proxytls.key",
        "test/qsealc/test-configuration.yaml",
    ],
    visibility = ["//visibility:public"],
)

load("@rules_codeowners//tools:codeowners.bzl", "codeowners")

codeowners(
    name = "other_files_owners",
    teams = [
        "@tink-ab/aggregation-fluffy-maintainer",
        "@tink-ab/aggregation-thundercats-maintainer",
        "@tink-ab/aggregation-minion-maintainer",
        "@tink-ab/aggregation-nazguls-maintainer",
        "@tink-ab/aggregation-teletubbies-maintainer",
        "@tink-ab/aggregation-llamas-maintainer",
        "@tink-ab/aggregation-penguins-maintainer",
        "@tink-ab/aggregation-toros-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "united_kingdom_owners",
    patterns = [
        "agents/uk/*",
        "secret/production/ss/uk/*",
        "secret/preprod/ss/uk/*",
        "secret/staging/ss/uk/*",
    ],
    teams = [
        "@tink-ab/aggregation-fluffy-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "ireland_owners",
    patterns = [
        "agents/ie/*",
        "secret/production/ss/ie/*",
        "secret/preprod/ss/ie/*",
        "secret/staging/ss/ie/*",
    ],
    teams = [
        "@tink-ab/aggregation-fluffy-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "estonian_owners",
    patterns = [
        "agents/ee/*",
        "secret/production/ss/ee/*",
        "secret/preprod/ss/ee/*",
        "secret/staging/ss/ee/*",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "latvian_owners",
    patterns = [
        "agents/lv/*",
        "secret/production/ss/lv/*",
        "secret/preprod/ss/lv/*",
        "secret/staging/ss/lv/*",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "lithuanian_owners",
    patterns = [
        "agents/lt/*",
        "secret/production/ss/lt/*",
        "secret/preprod/ss/lt/*",
        "secret/staging/ss/lt/*",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "netherland_owners",
    patterns = [
        "agents/nl/*",
        "secret/production/ss/nl/*",
        "secret/preprod/ss/nl/*",
        "secret/staging/ss/nl/*",
    ],
    teams = [
        "@tink-ab/aggregation-minion-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "swedish_owners",
    patterns = [
        "agents/se/*",
        "secret/production/ss/se/*",
        "secret/preprod/ss/se/*",
        "secret/staging/ss/se/*",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "belgian_owners",
    patterns = [
        "agents/be/*",
        "secret/production/ss/be/*",
        "secret/preprod/ss/be/*",
        "secret/staging/ss/be/*",
    ],
    teams = [
        "@tink-ab/aggregation-minion-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "portuguese_owners",
    patterns = [
        "agents/pt/*",
        "secret/production/ss/pt/*",
        "secret/preprod/ss/pt/*",
        "secret/staging/ss/pt/*",
    ],
    teams = [
        "@tink-ab/aggregation-minion-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "italian_owners",
    patterns = [
        "agents/it/*",
        "secret/production/ss/it/*",
        "secret/preprod/ss/it/*",
        "secret/staging/ss/it/*",
    ],
    teams = [
        "@tink-ab/aggregation-nazguls-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "german_owners",
    patterns = [
        "agents/de/*",
        "secret/production/ss/de/*",
        "secret/preprod/ss/de/*",
        "secret/staging/ss/de/*",
    ],
    teams = [
        "@tink-ab/aggregation-nazguls-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "danish_owners",
    patterns = [
        "agents/dk/*",
        "secret/production/ss/dk/*",
        "secret/preprod/ss/dk/*",
        "secret/staging/ss/dk/*",
    ],
    teams = [
        "@tink-ab/aggregation-teletubbies-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "norwegian_owners",
    patterns = [
        "agents/no/*",
        "secret/production/ss/no/*",
        "secret/preprod/ss/no/*",
        "secret/staging/ss/no/*",
    ],
    teams = [
        "@tink-ab/aggregation-teletubbies-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "finnish_owners",
    patterns = [
        "agents/fi/*",
        "secret/production/ss/fi/*",
        "secret/preprod/ss/fi/*",
        "secret/staging/ss/fi/*",
    ],
    teams = [
        "@tink-ab/aggregation-llamas-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "polish_owners",
    patterns = [
        "agents/pl/*",
        "secret/production/ss/pl/*",
        "secret/preprod/ss/pl/*",
        "secret/staging/ss/pl/*",
    ],
    teams = [
        "@tink-ab/aggregation-teletubbies-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "austrian_owners",
    patterns = [
        "agents/at/*",
        "secret/production/ss/at/*",
        "secret/preprod/ss/at/*",
        "secret/staging/ss/at/*",
    ],
    teams = [
        "@tink-ab/aggregation-penguins-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "french_owners",
    patterns = [
        "agents/fr/*",
        "secret/production/ss/fr/*",
        "secret/preprod/ss/fr/*",
        "secret/staging/ss/fr/*",
    ],
    teams = [
        "@tink-ab/aggregation-penguins-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "spanish_owners",
    patterns = [
        "agents/es/*",
        "secret/production/ss/es/*",
        "secret/preprod/ss/es/*",
        "secret/staging/ss/es/*",
    ],
    teams = [
        "@tink-ab/aggregation-toros-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
