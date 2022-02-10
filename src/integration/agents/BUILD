load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "agents",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        "@maven//:com_sun_xml_bind_jaxb_impl",
    ],
    deps = [
        "//src/integration/account/src/main/java/se/tink/libraries/account/enums",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/legacy/modules",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/legacy/modules/providers",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:demo",
        "//src/integration/credentials:enums",
        "//src/integration/credentials:service",
        "//src/integration/enums",
        "//src/integration/identity_data",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/module/annotation",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/jersey/filter",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/log",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/soap",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/integrations/abnamro",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/signaturekeypair",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/authentication",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/exceptions/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filterable/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/form",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/log/executor",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/models",
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation/enums",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//src/integration/user:rpc",
        "//src/libraries/agent-storage",
        "//src/libraries/credentials-persistence",
        "//src/libraries/date",
        "//src/libraries/encoding",
        "//src/libraries/giro_validation",
        "//src/libraries/i18n_aggregation",
        "//src/libraries/jollyday/src/main/java/se/tink/libraries/jollyday",
        "//src/libraries/metrics:collection",
        "//src/libraries/metrics:configuration",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:counters",
        "//src/libraries/metrics:gauges",
        "//src/libraries/metrics:histograms",
        "//src/libraries/metrics:logbackappender",
        "//src/libraries/metrics:monitor",
        "//src/libraries/metrics:other",
        "//src/libraries/metrics:prometheus",
        "//src/libraries/metrics:registry",
        "//src/libraries/metrics:timers",
        "//src/libraries/net/src/main/java/se/tink/libraries/net",
        "//src/libraries/net/src/main/java/se/tink/libraries/net/client",
        "//src/libraries/pair",
        "//src/libraries/phone_number_utils",
        "//src/libraries/serialization",
        "//src/libraries/social_security_number",
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "//third_party:com_auth0_java_jwt",
        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_guice",
        "//third_party:com_google_instrumentation_instrumentation_api",
        "//third_party:com_nimbusds_nimbus_jose_jwt",
        "//third_party:com_nimbusds_srp6a",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:commons_codec_commons_codec",
        "//third_party:commons_httpclient_commons_httpclient",
        "//third_party:commons_io_commons_io",
        "//third_party:io_dropwizard_dropwizard_client",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_jersey",
        "//third_party:io_dropwizard_dropwizard_metrics",
        "//third_party:io_vavr",
        "//third_party:javax_validation_validation_api",
        "//third_party:lombok_libs",
        "//third_party:net_minidev_json_smart",
        "//third_party:net_sourceforge_tess4j",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_apache_commons_commons_csv",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_apache_curator_curator_client",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_apache_pdfbox_pdfbox",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_bouncycastle_bcprov_jdk15on",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_json_json",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:xerces_xercesImpl",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_xml",
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_sun_xml_ws_jaxws_ri",
        "@aggregation//:com_sun_xml_ws_jaxws_rt",
        "@aggregation//:io_netty_netty_codec",
        "@aggregation//:io_netty_netty_codec_http",
        "@aggregation//:jakarta_xml_bind_jakarta_xml_bind_api",
        "@aggregation//:jakarta_xml_soap_jakarta_xml_soap_api",
        "@aggregation//:jakarta_xml_ws_jakarta_xml_ws_api",
        "@aggregation//:joda_time_joda_time",
        "@aggregation//:org_glassfish_jaxb_jaxb_runtime",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
        "@tink_backend//src/libraries/account_identifier",
    ],
)

junit_test(
    name = "agents_test",
    srcs = glob(["src/test/java/**/*.java"]),
    data = [
        "//data:agent_unit_tests",
        "//data:agents",
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:provider_configurations",
        "//data:security",
        "//data:tesseract_training_set",
        "//etc:development",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:ch_qos_logback_logback_classic",
    ],
    deps = [
        "//src/integration/account/src/main/java/se/tink/libraries/account/enums",
        "//src/integration/account/src/main/java/se/tink/libraries/account/rpc",
        "//src/integration/agents",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/demo/banks/bankid",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/demo/banks/psd2/redirect",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/entercard",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/nordeabase",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/v31",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/tools",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/utils/fixtures",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:demo",
        "//src/integration/credentials:enums",
        "//src/integration/credentials:service",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/enums",
        "//src/integration/enums",
        "//src/integration/identity_data",
        "//src/integration/lib",
        "//src/integration/lib:annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents/utils",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/signaturekeypair",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/configuration",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/configuration/iface",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/framework/validation",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filterable/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/context",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/legacy",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/utils/currency",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/integration/models",
        "//src/integration/payment:enums",
        "//src/integration/payment:rpc",
        "//src/integration/tpp_secrets_service/client/src/main/java/se/tink/backend/integration/tpp_secrets_service/client:tpp-secrets-service-client",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:mocks",
        "//src/integration/transfer:rpc",
        "//src/integration/user:rpc",
        "//src/libraries/account_data_cache",
        "//src/libraries/agent-storage",
        "//src/libraries/credentials-persistence",
        "//src/libraries/creditsafe_consumer_monitoring",
        "//src/libraries/date",
        "//src/libraries/encoding",
        "//src/libraries/i18n_aggregation",
        "//src/libraries/jollyday/src/main/java/se/tink/libraries/jollyday",
        "//src/libraries/serialization",
        "//src/libraries/social_security_number:social_security_number_test",
        "//src/libraries/social_security_number/src/test/java/se/tink/libraries/social/security/ssn",
        "//src/libraries/social_security_number/src/test/java/se/tink/libraries/social/security/time",
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_nimbusds_nimbus_jose_jwt",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:io_dropwizard_dropwizard_configuration",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_jersey",
        "//third_party:io_vavr",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_hamcrest_hamcrest_library",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_xml",
        "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_joda",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:joda_time_joda_time",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
        "@tink_backend//src/libraries/account_identifier",
        "@tink_backend//src/libraries/account_identifier/src/test/java/se/tink/libraries/account/identifiers/account",
    ],
)

codeowners(
    name = "common_agent_code_owners",
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
    name = "austrian_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/at/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/at/",
    ],
    teams = [
        "@tink-ab/aggregation-penguins-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "swedish_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/se/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/se/",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "uk_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/",
    ],
    teams = [
        "@tink-ab/aggregation-fluffy-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "estonian_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/ee/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/ee/",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "german_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/de/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/de/",
    ],
    teams = [
        "@tink-ab/aggregation-nazguls-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "latvian_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/lv/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/lv/",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "danish_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/dk/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/",
    ],
    teams = [
        "@tink-ab/aggregation-teletubbies-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "lithuanian_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/lt/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/lt/",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "belgian_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/be/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/be/",
    ],
    teams = [
        "@tink-ab/aggregation-minion-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "netherland_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/nl/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/",
        "src/main/java/se/tink/backend/aggregation/agents/legacy/abnamro/",
        "src/test/java/se/tink/backend/aggregation/agents/legacy/abnamro/",
    ],
    teams = [
        "@tink-ab/aggregation-minion-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "finnish_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/fi/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/",
    ],
    teams = [
        "@tink-ab/aggregation-llamas-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "legacy_banks_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/legacy/banks/",
        "src/test/java/se/tink/backend/aggregation/agents/legacy/banks/",
        "src/main/java/se/tink/backend/aggregation/agents/legacy/brokers/",
        "src/test/java/se/tink/backend/aggregation/agents/legacy/brokers/",
        "src/main/java/se/tink/backend/aggregation/agents/legacy/creditcards/",
        "src/test/java/se/tink/backend/aggregation/agents/legacy/creditcards/",
        "src/main/java/se/tink/backend/aggregation/agents/legacy/other/",
        "src/test/java/se/tink/backend/aggregation/agents/legacy/other/",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "french_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/",
    ],
    teams = [
        "@tink-ab/aggregation-penguins-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "creditcard_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/creditcards/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/creditcards/",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "norwegian_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/no/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/no/",
    ],
    teams = [
        "@tink-ab/aggregation-teletubbies-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "nordnet_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/brokers/nordnet/",
    ],
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "portuguese_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/pt/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/",
    ],
    teams = [
        "@tink-ab/aggregation-minion-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "polish_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/pl/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/pl/",
    ],
    teams = [
        "@tink-ab/aggregation-teletubbies-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "italian_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/it/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/it/",
    ],
    teams = [
        "@tink-ab/aggregation-nazguls-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "ie_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/ie/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/ie/",
    ],
    teams = [
        "@tink-ab/aggregation-fluffy-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "nordea_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/nordea/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/nordea/",
    ],
    teams = [
        "@tink-ab/aggregation-llamas-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "spanish_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/es/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/es/",
    ],
    teams = [
        "@tink-ab/aggregation-toros-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

codeowners(
    name = "demobank_providers_owners",
    patterns = [
        "src/main/java/se/tink/backend/aggregation/agents/nxgen/demo/",
        "src/test/java/se/tink/backend/aggregation/agents/nxgen/demo/",
    ],
    teams = [
        "@tink-ab/aggregation-coreagg-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
