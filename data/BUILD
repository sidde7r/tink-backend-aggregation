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
    srcs = glob(["test/*", "templates/*"]) + [
        "seeding/bank-fee-rules.txt",
        "seeding/counties-and-municipalities.json",
    ],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "main-lib-test",
    srcs = [
        "seeding/clients-abnamro.json",
        "seeding/clients-se.json",
    ],
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
    name = "system-lib-test",
    srcs = glob(["test/*"]) + [
        "seeding/cities-global.json",
        "seeding/cities-se.txt",
        "seeding/cities-nl.txt",
        "seeding/iso_countries.csv",
        "seeding/extrapolation-corpus-greedy.txt",
    ],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "aggregation-test",
    srcs = glob([
        "aggregation/**",
        "demo/**"
    ]),
    visibility = ["//visibility:public"],
)

filegroup(
    name = "integration-test",
    srcs = [
        "GeoIP2-Country.mmdb",
        "agents/seb/seb.p12",
        "agents/sbab/question-groups-input.json",
        "push/enterprise.p12",
        "search/search-mappings-transaction.json",
        "search/search-settings-transaction.json",
        "security/TinkCA.truststore",
        "security/abnamro.truststore",
        "seeding/cities-global.json",
        "seeding/cities-nl.txt",
        "seeding/cities-se.txt",
        "seeding/clients-se.json",
        "seeding/bank-fee-rules.txt",
        "seeding/extrapolation-corpus-greedy.txt",
        "seeding/iso_countries.csv",
        "templates/default-style.html",
        "templates/fraud/fraud-details-help-head.html",
        "templates/fraud/fraud-details-help-body.html",
        "templates/html-activity-format.html",
        "test/passwords.txt",
        "version.json",
        "demo/201212121212/accounts.txt",
        "demo/201212121212/9999-111111111111.txt",
        "demo/201212121212/9999-222222222222.txt",
        "security/csn.truststore"
    ],
    visibility = ["//:__pkg__"],
)

filegroup(
    name = "system-test",
    srcs = [
        "security/developer.keystore",
    ],
    visibility = ["//:__pkg__"],
)

filegroup(
  name = "fasttext-training-set",
  srcs = ["categorization/fasttext/minimal_model_train.txt"],
  visibility = ["//visibility:public"],
)

filegroup(
    name = "cryptography-test",
    srcs = [
        "test/cryptography/private_rsa_key.pem",
        "test/cryptography/public_rsa_key.pub"
    ],
    visibility = ["//visibility:public"],
)
