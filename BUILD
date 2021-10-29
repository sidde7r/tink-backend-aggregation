load("@rules_codeowners//tools:codeowners.bzl", "codeowners", "generate_codeowners")

package(default_visibility = ["//visibility:public"])

generate_codeowners(
    name = "generate_codeowners",
    owners = [
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
        ":workspace",
        "//src/integration/agents:austrian_providers_owners",
        "//src/integration/agents:belgian_providers_owners",
        "//src/integration/agents:french_providers_owners",
        "//src/integration/agents:portuguese_providers_owners",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http_api_client:owners",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen/http_api_client:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/n26:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/n26:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/n26:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/n26:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sibs:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sibs:owners",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/revolut:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/revolut:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/danskebank:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/danskebank:owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/aib:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/v31:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/amazon:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/aqua:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/argos:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/bankofscotland:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/barclays:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/bbank:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/burtonmenswear:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/capitalone:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/cashplus:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/clydesdale:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/common/openid:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/couttsandco:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/danskebank:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/debenhams:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/dorothyperkins:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/evans:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/firstdirect:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/firsttrust:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/fluid:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/halifax:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/houseoffraser:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/hsbc:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/lauraashley:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/lloyds:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/marbles:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/markandspencer:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/mbna:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/missselfridge:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/monzo:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/nationwide:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/natwest:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/opus:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/outfit:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/rbs:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/revolut:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/santander:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/starling:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/tesco:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/tide:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/topman:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/topshop:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/tsb:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ulster:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/vanquis:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/wallis:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/yorkshire:owners",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/pis:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/starling:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/handelsbanken:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/lansforsakringar:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/nordea/v30:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/handelsbanken:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/icabanken:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/sbab:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/sebopenbanking:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/skandia:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/nordeabase:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/hsbc:paycon-owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/icabanken:paycon-owners",
        "//src/integration/agents:spanish_providers_owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/redsys:owners",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/redsys:owners",
        "//src/integration/enums:integration_enums_owners",
        "//src/libraries/queue_sqs:queue_sqs_owners",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/resources:owners",
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
    name = "workspace",
    patterns = ["WORKSPACE"],
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
        "@tink-ab/aggregation-nazguls-maintainer",
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
        "@tink-ab/aggregation-teletubbies-maintainer",
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
        "@tink-ab/aggregation-thundercats-maintainer",
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
