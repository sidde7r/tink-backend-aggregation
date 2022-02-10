load("@rules_codeowners//tools:codeowners.bzl", "codeowners", "generate_codeowners")

package(default_visibility = ["//visibility:public"])

generate_codeowners(
    name = "generate_codeowners",
    owners = [
        "//src/integration/agents:common_agent_code_owners",
        "//agent_test_server:owners",
        "//aws_log_fetcher:owners",
        "//provider_tester:owners",
        "//src/aggregation:owners",
        "//src/integration/account/src/main/java/se/tink/libraries/account:owners",
        "//src/integration/agent_data_availability_tracker:owners",
        "//src/integration/amount:owners",
        "//src/integration/bankid:owners",
        "//src/integration/nemid:owners",
        "//src/integration/tpp_secrets_service:owners",
        "//src/integration/api/src:owners",
        "//src/integration/credentials:owners",
        "//src/integration/identity_data:owners",
        "//src/integration/lib:owners",
        "//src/integration/models:owners",
        "//src/integration/payment:owners",
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation:owners",
        "//src/integration/transfer:owners",
        "//src/integration/user:owners",
        "//src/integration/webdriver/src:owners",
        "//src/libraries:libraries_default_owners",
        "//third_party:owners",
        "//tools:owners",
        ":aws_log_fetcher_owners",
        ":charts_agent_platform",
        ":charts_precomputations_for_alerts",
        "//.buildkite:owners",
        ":charts_austrian_alerts",
        ":charts_belgian_alerts",
        ":charts_german_alerts",
        ":charts_danish_alerts",
        ":charts_estonian_alerts",
        ":charts_spanish_alerts",
        ":charts_finnish_alerts",
        ":charts_french_alerts",
        ":charts_british_alerts",
        ":charts_irish_alerts",
        ":charts_italian_alerts",
        ":charts_lithuanian_alerts",
        ":charts_latvian_alerts",
        ":charts_dutch_alerts",
        ":charts_norwegian_alerts",
        ":charts_polish_alerts",
        ":charts_portuguese_alerts",
        ":charts_swedish_alerts",
        ":docker_owners",
        ":workspace_and_root_build",
        "//data:other_files_owners",
        "//data:ireland_owners",
        "//data:united_kingdom_owners",
        "//data:danish_owners",
        "//data:estonian_owners",
        "//data:finnish_owners",
        "//data:latvian_owners",
        "//data:lithuanian_owners",
        "//data:netherland_owners",
        "//data:norwegian_owners",
        "//data:swedish_owners",
        "//data:belgian_owners",
        "//data:polish_owners",
        "//data:portuguese_owners",
        "//data:german_owners",
        "//data:italian_owners",
        "//data:austrian_owners",
        "//data:french_owners",
        "//data:spanish_owners",
        "//po:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bankdata:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bec:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bunq:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/creditagricole:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/crosskey:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/euroinformation:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/revolut:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/samlink:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/sdc:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bankdata:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bbva:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bec:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/citadele:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/fropenbanking/base:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/handelsbanken:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/luminor:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/samlink:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sebbaltics:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sebbrandedcards:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/swedbankbaltics/authentication:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/unicredit:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/ee:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fo/banks/sdcfo:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/mx/banks:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/ro/banks/raiffeisen:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bankdata:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bbva:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bec:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/berlingroup:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bnpparibasfortisbase:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/common/signature:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/fintechblocks:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/fropenbanking/base:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/newday:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/paypal:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/samlink:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/unicredit:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/xs2adevelopers:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/xs2adevelopers:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/ro/banks/raiffeisen:owners",
        "//src/integration/agents:austrian_providers_owners",
        "//src/integration/agents:belgian_providers_owners",
        "//src/integration/agents:french_providers_owners",
        "//src/integration/agents:portuguese_providers_owners",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http_api_client:owners",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen/http_api_client:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/n26:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/n26:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bnpparibas:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/creditagricole:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bpcegroup:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cmcic:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/n26:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/n26:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bnpparibas:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/creditagricole:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bpcegroup:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cmcic:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sibs:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sibs:owners",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock:owners",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/wiremock:owners",
        "//src/integration/agents:danish_providers_owners",
        "//src/integration/agents:demobank_providers_owners",
        "//src/integration/agents:finnish_providers_owners",
        "//src/integration/agents:norwegian_providers_owners",
        "//src/integration/agents:polish_providers_owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/targobank:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/crosskey:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/crosskey:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/danskebank:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/danskebank:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sdc:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sdc:owners",
        "//src/integration/agents:german_providers_owners",
        "//src/integration/agents:italian_providers_owners",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents:berlingroup_utils_owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bankverlag:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/deutschebank:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/fabric:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bankverlag:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/deutschebank:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/fabric:owners",
        "//src/integration/agents:uk_providers_owners",
        "//src/integration/agents:ie_providers_owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/polishapi:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/revolut:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/revolut:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/danskebank:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/danskebank:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/ukob/danskebank:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/v31:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/barclays:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/common:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/danskebank:owners",
        "//src/integration/agents:swedish_providers_owners",
        "//src/integration/agents:estonian_providers_owners",
        "//src/integration/agents:latvian_providers_owners",
        "//src/integration/agents:lithuanian_providers_owners",
        "//src/integration/agents:netherland_providers_owners",
        "//src/integration/agents:legacy_banks_providers_owners",
        "//src/integration/agents:creditcard_providers_owners",
        "//src/integration/agents:nordnet_providers_owners",
        "//src/integration/agents:nordea_providers_owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/luminor:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sebbase:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sebbase:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/swedbank:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/swedbank:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/swedbankbaltics:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/nordeabase:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/nordeabase:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/citadele:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/handelsbanken:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sebbaltics:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sebbrandedcards:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ingbase:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ingbase:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/entercard:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/amex:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/amex:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/fintecsystems:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/seb:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/handelsbanken:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/handelsbanken:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/demo/openbanking/demobank:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/pis:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/starling:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/ukob/hsbcgroup/hsbc:owners",
        "//src/integration/agents:spanish_providers_owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/redsys:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/redsys:owners",
        "//src/integration/enums:integration_enums_owners",
        "//src/libraries/queue_sqs:queue_sqs_owners",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/resources:owners",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/command:owners",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockpayment:owners",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockrefresh:owners",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen/propertiesloader:owners",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/propertiesloader:owners",
        "//src/agent_sdk:owners",
        "//:remaing_files_owners",
    ],
)

# If this test fails, copy the generated codeowners data to the CODEOWNERS file
sh_test(
    name = "validate_codeowners_up_to_date",
    srcs = ["@rules_codeowners//tools:diff.sh"],
    args = [
        "$(location :generate_codeowners.out)",
        "$(location CODEOWNERS)",
    ],
    data = [
        "CODEOWNERS",
        ":generate_codeowners.out",
    ],
)

codeowners(
    name = "aws_log_fetcher_owners",
    pattern = "/aws_log_fetcher/",
    teams = [
        "@tink-ab/aggregation-agent-platform-maintainer",
    ],
)

codeowners(
    name = "workspace_and_root_build",
    patterns = [
        "WORKSPACE",
        "BUILD",
    ],
    teams = [
        "@tink-ab/aggregation-agent-platform-maintainer",
    ],
)

codeowners(
    name = "charts_agent_platform",
    patterns = [
        "/.charts",
    ],
    teams = [
        "@tink-ab/aggregation-agent-platform-maintainer",
    ],
)

codeowners(
    name = "charts_precomputations_for_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/precomputations.yaml",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
)

codeowners(
    name = "charts_austrian_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/at",
    ],
    teams = [
        "@tink-ab/aggregation-penguins-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_belgian_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/be",
    ],
    teams = [
        "@tink-ab/aggregation-minion-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_german_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/de",
    ],
    teams = [
        "@tink-ab/aggregation-nazguls-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_danish_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/dk",
    ],
    teams = [
        "@tink-ab/aggregation-teletubbies-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_estonian_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/ee",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_spanish_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/es",
    ],
    teams = [
        "@tink-ab/aggregation-toros-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_finnish_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/fi",
    ],
    teams = [
        "@tink-ab/aggregation-llamas-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_french_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/fr",
    ],
    teams = [
        "@tink-ab/aggregation-penguins-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_british_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/gb",
    ],
    teams = [
        "@tink-ab/aggregation-fluffy-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_irish_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/ie",
    ],
    teams = [
        "@tink-ab/aggregation-fluffy-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_italian_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/it",
    ],
    teams = [
        "@tink-ab/aggregation-nazguls-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_lithuanian_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/lt",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_latvian_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/lv",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_dutch_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/nl",
    ],
    teams = [
        "@tink-ab/aggregation-minion-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_norwegian_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/no",
    ],
    teams = [
        "@tink-ab/aggregation-teletubbies-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_polish_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/pl",
    ],
    teams = [
        "@tink-ab/aggregation-teletubbies-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_portuguese_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/pt",
    ],
    teams = [
        "@tink-ab/aggregation-minion-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "charts_swedish_alerts",
    patterns = [
        "/.charts/tink-backend-aggregation-agents/templates/se",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "docker_owners",
    patterns = [
        "/docker",
    ],
    teams = [
        "@tink-ab/aggregation-agent-platform-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "remaing_files_owners",
    patterns = [
        "bors.toml",
        "sonar-project.properties",
        "jobs/cron/demo-bank/cronjob.py",
        "jobs/cron/demo-bank/requirements.txt",
        "jobs/cron/demo-bank/Dockerfile",
        "jobs/cron/connectivity/cronjob.py",
        "jobs/cron/connectivity/requirements.txt",
        "jobs/cron/connectivity/Dockerfile",
        "jobs/cron/provider-status/cronjob.py",
        "jobs/cron/provider-status/requirements.txt",
        "jobs/cron/provider-status/Dockerfile",
        ".secrets.baseline",
        ".dockerignore",
        ".bazelproject",
        ".bazelrc",
        "CODEOWNERS",
        "RunAggregation.sh",
        "etc/test.yml",
        "etc/dummy_secrets_adder.py",
        "etc/nasa/development-sensitive-configuration.yaml",
        "etc/nasa/development-configuration.yaml",
        "etc/dummy_values_dict.json",
        "etc/integration/development-sensitive-configuration.yaml",
        "etc/integration/development-configuration.yaml",
        "etc/logback-test.xml",
        ".bazelversion",
        "tink_ide_settings/run/All_Tests.xml",
        "tink_ide_settings/run/Run_Aggregation_Decoupled.xml",
        "tink_ide_settings/run/Most_Tests.xml",
        "tink_ide_settings/run/Minikube-Server__Aggregation.xml",
        "stamp.sh",
        "kubernetes/development/aggregationdb.yml",
        "kubernetes/development/coordination.yml",
        "kubernetes/development/providerdb.yml",
        "kubernetes/development/cache.yml",
        ".forego",
        "bazel-wrapper",
        "src/pom-service-parent.xml",
    ],
    teams = [
        "@tink-ab/aggregation-agent-platform-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

alias(
    name = "aggregation",
    actual = "//src/aggregation/service:bin",
)

alias(
    name = "convert_s3_to_aap",
    actual = "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/converter",
)

# Files needed by manual agent tests
filegroup(
    name = "agent_test_data",
    srcs = [
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
)

test_suite(
    name = "most_tests",
    tests = [
        "//src/aggregation/api:api_test",
        "//src/aggregation/lib/src/test/java/se/tink/backend/aggregation/wrappers",
        "//src/integration/lib:lib_test",
    ],
)

alias(
    name = "fmt",
    actual = "//tools/format:fmt",
    visibility = ["//:__pkg__"],
)

alias(
    name = "format",
    actual = "//tools/format/java:format-java-diff",
)

alias(
    name = "formatjson",
    actual = "//tools/format/json:format-json-diff",
)

# Workaround for annoying "no such target '//:.bazelproject'" error in recent plugin versions
exports_files([".bazelproject"])
