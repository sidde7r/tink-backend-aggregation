load("//tools/bzl:junit.bzl", "junit_test")
load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

# TODO: move these into their own component directories when the modules
# have been merged to suitable components.

java_library(
    name = "aggregation-api",
    srcs = glob(["src/aggregation-api/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        ":main-api",
        ":system-api",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/api_annotations:api-annotations",
        "//src/libraries/creditsafe_consumer_monitoring",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/http:http-annotations",
        "//src/libraries/http_client:http-client",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/metrics",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:commons_codec_commons_codec",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:javax_validation_validation_api",
        "//third_party:joda_time_joda_time",
        "//third_party:org_hibernate_hibernate_annotations",
        "//third_party:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
    ],
)

java_library(
    name = "system-api",
    srcs = glob(["src/system-api/src/main/**/*.java"]),
    deps = [
        ":main-api",
        "//src/libraries/api_annotations:api-annotations",
        "//src/libraries/date",
        "//src/libraries/http:http-annotations",
        "//src/libraries/http_client:http-client",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/uuid",
        "//third_party:c3p0_c3p0",
        "//third_party:com_datastax_cassandra_cassandra_driver_core",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:com_restfb_restfb",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:io_reactivex_rxjava",
        "//third_party:jfree_jcommon",
        "//third_party:org_jfree_jfreesvg",
        "//third_party:org_rythmengine_rythm_engine",
    ],
)

java_library(
    name = "main-api",
    srcs = glob(["src/main-api/src/main/**/*.java"]),
    resources = glob(["src/main-api/src/main/resources/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "//src/libraries/http:http-annotations",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/auth",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/http_client:http-client",
        "//src/libraries/date",
        "//src/libraries/giro_validation:giro-validation",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/i18n",
        "//src/libraries/log",
        "//src/libraries/phone_number_utils",
        "//src/libraries/api_annotations:api-annotations",
        "//src/libraries/api_headers:api-headers",

        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:com_google_guava_guava",
        "//third_party:com_googlecode_concurrent_trees_concurrent_trees",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:commons_beanutils_commons_beanutils",
        "//third_party:commons_io_commons_io",
        "//third_party:commons_lang_commons_lang",
        "//third_party:de_jollyday_jollyday",
        "//third_party:io_protostuff_protostuff_api",
        "//third_party:io_protostuff_protostuff_core",
        "//third_party:io_protostuff_protostuff_runtime",
        "//third_party:io_swagger_swagger_annotations",
        "//third_party:javax_validation_validation_api",
        "//third_party:joda_time_joda_time",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_mahout_mahout_math",
        "//third_party:org_apache_maven_maven_artifact",
        "//third_party:org_eclipse_jetty_orbit_javax_servlet",
        "//third_party:org_hibernate_hibernate_annotations",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_pojava_pojava",
        "//third_party:org_xerial_snappy_snappy_java",
        "//third_party:commons_codec_commons_codec",
        "//third_party:com_lambdaworks_scrypt",
        "//third_party:org_json_json",
    ],
)

java_library(
    name = "aggregationcontroller-api",
    srcs = glob(["src/aggregationcontroller-api/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        ":aggregation-api",
        ":main-api",
        ":system-api",
        "//src/libraries/api_annotations:api-annotations",
        "//src/libraries/http:http-annotations",
        "//src/libraries/http_client:http-client",
        "//src/libraries/jersey_utils:jersey-utils",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
    ],
)

java_library(
    name = "aggregation-service",
    srcs = glob(["src/aggregation-service/src/main/**/*.java"]),
    data = [
        "//data",
    ],
    deps = [
        ":agents-lib",
        ":aggregation-api",
        ":aggregation-lib",
        ":aggregationcontroller-api",
        ":main-api",
        ":system-api",
        "//src/libraries/auth",
        "//src/libraries/discovery",
        "//src/libraries/draining",
        "//src/libraries/dropwizard_utils:dropwizard-utils",
        "//src/libraries/metrics",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_netflix_governator",
        "//third_party:io_dropwizard_dropwizard_core",
    ],
)

java_library(
    name = "aggregation-lib",
    srcs = glob(["src/aggregation-lib/src/main/**/*.java"]),
    data = [
        "//data:cryptography-test",
        "//data:tesseract-training-set",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
    ],
    resources = glob(["src/aggregation-lib/src/main/resources/**"]),
    visibility = ["//visibility:public"],
    deps = [
        ":agents-lib",
        ":aggregation-api",
        ":aggregationcontroller-api",
        ":main-api",
        ":provider-configuration-api",
        ":system-api",
        "//po:i18n",
        "//src/libraries/abnamro:abn_amro",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/api_annotations:api-annotations",
        "//src/libraries/cache",
        "//src/libraries/cli_print_utils",
        "//src/libraries/concurrency",
        "//src/libraries/creditsafe_consumer_monitoring",
        "//src/libraries/date",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/discovered_web_service",
        "//src/libraries/discovery",
        "//src/libraries/draining",
        "//src/libraries/executor_utils:executor-utils",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/giro_validation:giro-validation",
        "//src/libraries/http:http-annotations",
        "//src/libraries/http:http-utils",
        "//src/libraries/http_client:http-client",
        "//src/libraries/i18n",
        "//src/libraries/jersey_guice:jersey-guice",
        "//src/libraries/jersey_log:jersey-log",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/log",
        "//src/libraries/metrics",
        "//src/libraries/net",
        "//src/libraries/pair",
        "//src/libraries/phone_number_utils",
        "//src/libraries/queue-lib",
        "//src/libraries/queue-sqs",
        "//src/libraries/repository",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/service_version:service-version",
        "//src/libraries/social_security_number:social-security-number",
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "//third_party:com_amazonaws_aws_java_sdk_s3",
        "//third_party:com_amazonaws_aws_java_sdk_sqs",
        "//third_party:com_auth0_java_jwt",
        "//third_party:com_fasterxml_jackson_dataformat_jackson_dataformat_xml",
        "//third_party:com_github_detro_ghostdriver_phantomjsdriver",
        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:com_google_code_gson_gson",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_extensions_guice_multibindings",
        "//third_party:com_google_inject_guice",
        "//third_party:com_lambdaworks_scrypt",
        "//third_party:com_nimbusds_srp6a",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:commons_httpclient_commons_httpclient",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:joda_time_joda_time",
        "//third_party:net_sourceforge_cssparser_cssparser",
        "//third_party:net_sourceforge_tess4j",
        "//third_party:net_spy_spymemcached",
        "//third_party:org_apache_commons_commons_math3",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_recipes",
        "//third_party:org_apache_lucene_lucene_core",
        "//third_party:org_apache_pdfbox_fontbox",
        "//third_party:org_apache_pdfbox_pdfbox",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_codehaus_jackson_jackson_core_asl",
        "//third_party:org_hibernate_hibernate_annotations",
        "//third_party:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_mozilla_rhino",
        "//third_party:org_seleniumhq_selenium_selenium_java",
        "//third_party:org_springframework_data_spring_data_jpa",
        "//third_party:org_springframework_spring_core",
        "//third_party:org_springframework_spring_expression",
        "//third_party:org_xerial_snappy_snappy_java",
    ],
)

java_library(
    name = "agents-lib",
    srcs = glob(["src/agents-lib/src/main/**/*.java"]),
    data = [
        "//data:tesseract-training-set",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
    ],
    visibility = ["//visibility:public"],
    deps = [
        ":aggregation-api",
        ":aggregationcontroller-api",
        ":main-api",
        ":system-api",
        "//src/libraries/abnamro:abn_amro",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/api_annotations:api-annotations",
        "//src/libraries/cache",
        "//src/libraries/cli_print_utils",
        "//src/libraries/cryptography",
        "//src/libraries/date",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/discovered_web_service",
        "//src/libraries/discovery",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/giro_validation:giro-validation",
        "//src/libraries/http:http-annotations",
        "//src/libraries/http:http-utils",
        "//src/libraries/http_client:http-client",
        "//src/libraries/i18n",
        "//src/libraries/jersey_guice:jersey-guice",
        "//src/libraries/jersey_log:jersey-log",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/log",
        "//src/libraries/metrics",
        "//src/libraries/net",
        "//src/libraries/pair",
        "//src/libraries/phone_number_utils",
        "//src/libraries/queue-lib",
        "//src/libraries/queue-sqs",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/social_security_number:social-security-number",
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "//third_party:com_amazonaws_aws_java_sdk_s3",
        "//third_party:com_auth0_java_jwt",
        "//third_party:com_fasterxml_jackson_dataformat_jackson_dataformat_xml",
        "//third_party:com_github_detro_ghostdriver_phantomjsdriver",
        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_extensions_guice_multibindings",
        "//third_party:com_google_inject_guice",
        "//third_party:com_lambdaworks_scrypt",
        "//third_party:com_nimbusds_srp6a",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:commons_httpclient_commons_httpclient",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:joda_time_joda_time",
        "//third_party:net_sourceforge_cssparser_cssparser",
        "//third_party:net_sourceforge_tess4j",
        "//third_party:org_apache_commons_commons_csv",
        "//third_party:org_apache_commons_commons_math3",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_recipes",
        "//third_party:org_apache_lucene_lucene_core",
        "//third_party:org_apache_pdfbox_fontbox",
        "//third_party:org_apache_pdfbox_pdfbox",
        "//third_party:org_apache_xmlgraphics_batik_css",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_codehaus_jackson_jackson_core_asl",
        "//third_party:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_modelmapper_modelmapper",
        "//third_party:org_mozilla_rhino",
        "//third_party:org_seleniumhq_selenium_selenium_java",
        "//third_party:org_springframework_data_spring_data_jpa",
        "//third_party:org_springframework_spring_core",
        "//third_party:org_springframework_spring_expression",
    ],
)

java_binary(
    name = "aggregation",
    srcs = glob(["src/aggregation-service/src/main/**/*.java"]),
    data = [
        "etc/development-aggregation-server.yml",
        "etc/development-minikube-aggregation-server.yml",
        "//data",
    ],
    main_class = "se.tink.backend.aggregation.AggregationServiceContainer",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:mysql_mysql_connector_java",
    ],
    deps = [
        ":agents-lib",
        ":aggregation-api",
        ":aggregation-lib",
        ":aggregationcontroller-api",
        ":main-api",
        "//src/libraries/auth",
        "//src/libraries/discovery",
        "//src/libraries/draining",
        "//src/libraries/dropwizard_utils:dropwizard-utils",
        "//src/libraries/metrics",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_netflix_governator",
        "//third_party:io_dropwizard_dropwizard_core",
    ],
)

genrule(
    name = "renamed-aggregation-deploy-jar",
    srcs = [":aggregation_deploy.jar"],
    outs = ["aggregation-service.jar"],
    cmd = "cp $(location :aggregation_deploy.jar) \"$(@)\"",
    visibility = ["//visibility:public"],
)

junit_test(
    name = "aggregation-api-test",
    srcs = glob(["src/aggregation-api/src/test/**/*.java"]) + [
    ],
    data = ["//data:tink-ca"],
    runtime_deps = [
        "//third_party:ch_qos_logback_logback_classic",
    ],
    deps = [
        ":aggregation-api",
        ":main-api",
        "//src/libraries/api_annotations:api-annotations",
        "//src/libraries/api_annotations:api-annotations-testlib-test",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_javassist_javassist",
        "//third_party:org_reflections_reflections",
    ],
)

junit_test(
    name = "aggregation-lib-test",
    srcs = glob(["src/aggregation-lib/src/test/**/*.java"]),
    data = [
        "//data:agents",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
    ],
    runtime_deps = [
        "//third_party:ch_qos_logback_logback_classic",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        ":agents-lib",
        ":aggregation-api",
        ":aggregation-lib",
        ":main-api",
        ":system-api",
        "//src/libraries/abnamro:abn_amro",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/concurrency",
        "//src/libraries/concurrency:concurrency-test",
        "//src/libraries/creditsafe_consumer_monitoring",
        "//src/libraries/date",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/giro_validation:giro-validation",
        "//src/libraries/i18n",
        "//src/libraries/log",
        "//src/libraries/metrics",
        "//src/libraries/net",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/strings",
        "//src/libraries/transfer_stub:transfer-stub-test",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_guice",
        "//third_party:com_nimbusds_srp6a",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:commons_io_commons_io",
        "//third_party:commons_lang_commons_lang",
        "//third_party:joda_time_joda_time",
        "//third_party:org_apache_pdfbox_pdfbox",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcprov_jdk15on",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)

junit_test(
    name = "agents-lib-test",
    srcs = glob(["src/agents-lib/src/test/**/*.java"]),
    data = [
        "//data:agents",
        "//data:cryptography-test",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
    ],
    runtime_deps = [
        "//third_party:ch_qos_logback_logback_classic",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        ":agents-lib",
        ":aggregation-api",
        ":main-api",
        ":system-api",
        "//src/libraries/abnamro:abn_amro",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/account_identifier:account-identifier-test",
        "//src/libraries/cryptography",
        "//src/libraries/date",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/giro_validation:giro-validation",
        "//src/libraries/i18n",
        "//src/libraries/log",
        "//src/libraries/metrics",
        "//src/libraries/net",
        "//src/libraries/pair",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/social_security_number:social-security-number-test",
        "//src/libraries/strings",
        "//src/libraries/transfer_stub:transfer-stub-test",
        "//third_party:com_auth0_java_jwt",
        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_guice",
        "//third_party:com_nimbusds_srp6a",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:commons_io_commons_io",
        "//third_party:commons_lang_commons_lang",
        "//third_party:joda_time_joda_time",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_apache_pdfbox_pdfbox",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcprov_jdk15on",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)

junit_test(
    name = "aggregation-test",
    size = "large",
    srcs = glob(["src/aggregation-tests/src/test/**/*.java"]),
    data = [
        "etc/development.yml",
        "//data:agent-unit-tests",
        "//data:agents",
        "//data:aggregation-test",
        "//data:cryptography-test",
        "//data:tesseract-training-set",
    ],
    tags = [
        "external",
        "manual",
    ],
    deps = [
        ":agents-lib",
        ":aggregation-api",
        ":aggregation-lib",
        ":main-api",
        ":system-api",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/account_identifier:account-identifier-test",
        "//src/libraries/creditsafe_consumer_monitoring",
        "//src/libraries/date",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/i18n",
        "//src/libraries/log",
        "//src/libraries/metrics",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/social_security_number:social-security-number",
        "//src/libraries/social_security_number:social-security-number-test",
        "//src/libraries/strings",
        "//src/libraries/transfer_stub:transfer-stub-test",
        "//src/libraries/uuid",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:commons_io_commons_io",
        "//third_party:io_dropwizard_dropwizard_configuration",
        "//third_party:io_dropwizard_dropwizard_jackson",
        "//third_party:javax_validation_validation_api",
        "//third_party:joda_time_joda_time",
        "//third_party:junit_junit",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:net_sourceforge_tess4j",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_hamcrest_hamcrest_library",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_objenesis_objenesis",
        "//third_party:org_seleniumhq_selenium_selenium_java",
    ],
)

java_library(
    name = "provider-configuration-api",
    srcs = glob(["src/provider-configuration-api/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        ":main-api",
        "//src/libraries/api_annotations:api-annotations",
        "//src/libraries/http:http-annotations",
        "//src/libraries/http_client:http-client",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/serialization_utils:serialization-utils",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:javax_validation_validation_api",
    ],
)

java_library(
    name = "provider-configuration-lib",
    srcs = glob(["src/provider-configuration-lib/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        ":main-api",
        ":provider-configuration-api",
        "//src/libraries/api_annotations:api-annotations",
        "//src/libraries/http:http-utils",
        "//src/libraries/http_client:http-client",
        "//src/libraries/i18n",
        "//src/libraries/jersey_guice:jersey-guice",
        "//src/libraries/jersey_log:jersey-log",
        "//src/libraries/metrics",
        "//src/libraries/repository",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/service_version:service-version",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_jersey",
        "//third_party:javax_validation_validation_api",
        "//third_party:org_eclipse_jetty_orbit_javax_servlet",
        "//third_party:org_hibernate_hibernate_annotations",
        "//third_party:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
        "//third_party:org_springframework_data_spring_data_commons",
        "//third_party:org_springframework_data_spring_data_jpa",
        "//third_party:org_springframework_spring_core",
        "//third_party:org_springframework_spring_expression",
    ],
)

java_library(
    name = "provider-configuration-service",
    srcs = glob(["src/provider-configuration-service/src/main/**/*.java"]),
    data = [
        "//data",
    ],
    deps = [
        ":main-api",
        ":provider-configuration-api",
        ":provider-configuration-lib",
        "//src/libraries/auth",
        "//src/libraries/cli_print_utils",
        "//src/libraries/discovery",
        "//src/libraries/dropwizard_utils:dropwizard-utils",
        "//src/libraries/metrics",
        "//src/libraries/repository",
        "//src/libraries/serialization_utils:serialization-utils",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_springframework_data_spring_data_jpa",
        "//third_party:org_springframework_spring_expression",
    ],
)

junit_test(
    name = "provider-configuration-lib-test",
    srcs = glob(["src/provider-configuration-lib/src/test/**/*.java"]),
    data = [
        "etc/development-provider-configuration-server.yml",
        "//data:provider-configuration-test",
    ],
    visibility = ["//visibility:public"],
    deps = [
        ":agents-lib",
        ":main-api",
        ":provider-configuration-api",
        ":provider-configuration-lib",
        "//src/libraries/metrics",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:io_dropwizard_dropwizard_configuration",
        "//third_party:javax_validation_validation_api",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_objenesis_objenesis",
    ],
)

java_binary(
    name = "provider-configuration",
    srcs = glob(["src/provider-configuration-service/src/main/**/*.java"]),
    data = [
        "etc/development-minikube-provider-configuration-server.yml",
        "etc/development-provider-configuration-server.yml",
        "//data",
    ],
    main_class = "se.tink.backend.aggregation.provider.ProviderConfigurationServiceContainer",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:mysql_mysql_connector_java",
    ],
    deps = [
        ":main-api",
        ":provider-configuration-api",
        ":provider-configuration-lib",
        "//src/libraries/auth",
        "//src/libraries/cli_print_utils",
        "//src/libraries/console_json_logger",
        "//src/libraries/discovery",
        "//src/libraries/dropwizard_utils:dropwizard-utils",
        "//src/libraries/metrics",
        "//src/libraries/repository",
        "//src/libraries/serialization_utils:serialization-utils",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_springframework_data_spring_data_jpa",
        "//third_party:org_springframework_spring_expression",
    ],
)

genrule(
    name = "renamed-provider-configuration-deploy-jar",
    srcs = [":provider-configuration_deploy.jar"],
    outs = ["provider-configuration-service.jar"],
    cmd = "cp $(location :provider-configuration_deploy.jar) \"$(@)\"",
    visibility = ["//visibility:public"],
)
