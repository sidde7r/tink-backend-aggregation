load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bankverlag",
    srcs = glob(["**/*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bankverlag/resources",
    ],
    tags = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bankverlag",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/agents/componentproviders",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/refresh",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/utils",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/account",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filterable/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/libraries/agent-storage",
        "//src/libraries/i18n_aggregation",
        "//src/libraries/serialization",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@tink_backend//src/libraries/account_identifier",
    ],
)

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-nazguls-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
