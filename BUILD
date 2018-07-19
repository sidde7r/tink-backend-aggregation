load("//tools/bzl:junit.bzl", "junit_test")
load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

# TODO: move these into their own component directories when the modules
# have been merged to suitable components.

java_library(
    name = "encryption-api",
    srcs = glob(["src/encryption-api/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/http:http-annotations",
        "//src/libraries/http_client:http-client",
        "//src/libraries/jersey_utils:jersey-utils",

        "//third_party:com_sun_jersey_jersey_client",
    ],
)

java_library(
    name = "aggregation-api",
    srcs = glob(["src/aggregation-api/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        ":main-api",
        ":system-api",

        "//src/libraries/metrics",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/http:http-annotations",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/http_client:http-client",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/strings:strings",
        "//src/libraries/uuid:uuid",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/api-annotations",

        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:commons_codec_commons_codec",
        "//third_party:joda_time_joda_time",
        "//third_party:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
        "//third_party:org_hibernate_hibernate_annotations",
        "//third_party:javax_validation_validation_api",
    ],
)

java_library(
    name = "system-api",
    srcs = glob(["src/system-api/src/main/**/*.java"]),
    deps = [
        ":main-api",

        "//src/libraries/http:http-annotations",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/http_client:http-client",
        "//src/libraries/uuid:uuid",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/date:date",
        "//src/api-annotations",

        "//third_party:c3p0_c3p0",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:com_restfb_restfb",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:io_reactivex_rxjava",
        "//third_party:jfree_jcommon",
        "//third_party:org_jfree_jfreesvg",
        "//third_party:org_rythmengine_rythm_engine",
        "//third_party:com_datastax_cassandra_cassandra_driver_core",
    ],
)

java_library(
    name = "main-api",
    srcs = glob(["src/main-api/src/main/**/*.java"]),
    resources = glob(["src/main-api/src/main/resources/**"]),
    deps = [
        "//src/libraries/strings:strings",
        "//src/libraries/uuid:uuid",
        "//src/libraries/http:http-annotations",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/auth:auth",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/http_client:http-client",
        "//src/libraries/date:date",
        "//src/libraries/cluster:cluster",
        "//src/libraries/giro_validation:giro-validation",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/versioning:versioning",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/i18n",
        "//src/libraries/log",
        "//src/libraries/math",
        "//src/libraries/phone_number_utils:phone_number_utils",
        "//src/libraries/oauth",
        "//src/api-annotations",
        "//src/api-headers",

        # Proto serialization util
        "//src/libraries/protobuf_serialization_utils:protobuf-serialization-utils",

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
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:commons_codec_commons_codec",
        "//third_party:com_lambdaworks_scrypt",
        "//third_party:org_json_json",
    ],
    visibility = ["//visibility:public"],
)

java_library(
    name = "aggregationcontroller-api",
    srcs = glob(["src/aggregationcontroller-api/src/main/**/*.java"]),
    deps = [
        ":main-api",
        ":system-api",
        ":aggregation-api",
        ":common-lib",
        "//src/api-annotations",
        "//src/cluster-lib:cluster-lib",
        "//src/libraries/discovery:discovery",
        "//src/libraries/discovered_web_service:discovered_web_service",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/http_client:http-client",
        "//src/libraries/endpoint_configuration:endpoint_configuration",
        "//src/libraries/http:http-annotations",
        "//third_party:com_google_inject_guice",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
    ],
    visibility = ["//visibility:public"],
)

java_library(
    name = "common-lib",
    srcs = glob(["src/common-lib/src/main/**/*.java"]),
    deps = [
        ":aggregation-api",
        ":encryption-api",
        ":main-api",
        ":system-api",

        "//src/libraries/http:http-annotations",
        "//src/libraries/http:http-utils",
        "//src/libraries/auth:auth",
        "//src/libraries/discovery:discovery",
        "//src/libraries/uuid:uuid",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/endpoint_configuration:endpoint_configuration",
        "//src/libraries/discovered_web_service:discovered_web_service",
        "//src/libraries/metrics:metrics",
        "//src/libraries/jersey_log:jersey-log",
        "//src/libraries/http_client:http-client",
        "//src/libraries/dropwizard_utils:dropwizard-utils",
        "//src/libraries/date:date",
        "//src/libraries/cluster:cluster",
        "//src/libraries/giro_validation:giro-validation",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/versioning:versioning",
        "//src/libraries/cassandra-interfaces",
        "//src/libraries/net",
        "//src/libraries/i18n",
        "//src/libraries/abnamro:abn_amro",
        "//src/libraries/identity:identity",
        "//src/libraries/strings:strings",
        "//src/api-annotations",

        "//src/api-headers",

        # Proto serialization util
        "//src/libraries/protobuf_serialization_utils:protobuf-serialization-utils",

        "//po:i18n",

        "//third_party:apporiented_com_hierarchical_clustering",
        "//third_party:com_datastax_cassandra_cassandra_driver_core",
        "//third_party:com_github_jknack_handlebars",
        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_guice",
        "//third_party:com_googlecode_concurrent_trees_concurrent_trees",
        "//third_party:com_googlecode_gettext_commons_gettext_commons",
        "//third_party:com_mandrillapp_wrapper_lutung_lutung",
        "//third_party:com_turo_pushy",
        "//third_party:com_restfb_restfb",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_yubico_yubico_validation_client2",
        "//third_party:commons_pool_commons_pool",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_intercom_intercom_java",
        "//third_party:io_netty_netty_common",
        "//third_party:io_prometheus_simpleclient",
        "//third_party:io_prometheus_simpleclient_hotspot",
        "//third_party:io_prometheus_simpleclient_servlet",
        "//third_party:io_protostuff_protostuff_collectionschema",
        "//third_party:io_protostuff_protostuff_runtime",
        "//third_party:io_reactivex_rxjava",
        "//third_party:io_swagger_swagger_jaxrs",
        "//third_party:jfree_jcommon",
        "//third_party:joda_time_joda_time",
        "//third_party:net_spy_spymemcached",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_apache_commons_commons_csv",
        "//third_party:org_apache_commons_commons_math",
        "//third_party:org_apache_commons_commons_math3",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_kafka_kafka_clients",
        "//third_party:org_apache_kafka_kafka_streams",
        "//third_party:com_sproutsocial_nsqj_j",
        "//third_party:org_apache_lucene_lucene_core",
        "//third_party:org_apache_xmlgraphics_batik_css",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_codehaus_jackson_jackson_core_asl",
        "//third_party:org_codehaus_plexus_plexus_utils",
        "//third_party:org_elasticsearch_elasticsearch",
        "//third_party:org_hibernate_hibernate_entitymanager",
        "//third_party:org_jfree_jfreesvg",
        "//third_party:org_json_json",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_modelmapper_modelmapper",
        "//third_party:org_pojava_pojava",
        "//third_party:org_quartz_scheduler_quartz",
        "//third_party:org_rythmengine_rythm_engine",
        "//third_party:org_springframework_data_spring_data_commons",
        "//third_party:org_springframework_data_spring_data_jpa",
        "//third_party:org_assertj_assertj_core",
    ],
    visibility = ["//visibility:public"],
)

java_library(
    name = "common-lib-testlib",
    srcs = glob(
        include = ["src/common-lib/src/test/**/*.java"],
        exclude = ["src/common-lib/src/test/**/*Test.java"],
    ),
    runtime_deps = [
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:pl_pragmatists_junitparams",
    ],
    deps = [
        ":common-lib",
        ":main-api",

        "//src/libraries/uuid:uuid",
        "//src/libraries/http:http-annotations",
        "//src/libraries/metrics:metrics",
        "//src/libraries/date:date",
        "//src/libraries/account_identifier:account-identifier",

        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:commons_lang_commons_lang",
        "//third_party:joda_time_joda_time",
        "//third_party:junit_junit",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_springframework_data_spring_data_commons",
    ],
)

java_library(
     name = "aggregation-service",
     srcs = glob(["src/aggregation-service/src/main/**/*.java"]),
     data = [
         "//data",
     ],
     deps = [
         ":aggregation-api",
         ":aggregation-lib",
         ":common-lib",
         ":encryption-api",
         ":main-api",
         ":system-api",
         ":agents-lib",
         ":aggregationcontroller-api",

          "//src/libraries/auth:auth",
          "//src/libraries/discovery:discovery",
          "//src/libraries/dropwizard_utils:dropwizard-utils",
          "//src/libraries/metrics:metrics",
          "//src/libraries/cluster:cluster",

         "//third_party:com_google_guava_guava",
         "//third_party:com_google_inject_guice",
         "//third_party:com_netflix_governator",
         "//third_party:io_dropwizard_dropwizard_core",
     ],
)

java_library(
    name = "aggregation-lib",
    srcs = glob(["src/aggregation-lib/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    data = [
        "//tools:phantomjs_mac",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
        "//data:tesseract-training-set",
    ],
    deps = [
        ":aggregation-api",
        ":common-lib",
        ":encryption-api",
        ":main-api",
        ":system-api",
        "agents-lib",
        ":aggregationcontroller-api",

        "//src/libraries/uuid:uuid",
        "//src/libraries/http:http-utils",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/discovery:discovery",
        "//src/libraries/metrics:metrics",
        "//src/libraries/jersey_guice:jersey-guice",
        "//src/libraries/jersey_log:jersey-log",
        "//src/libraries/date:date",
        "//src/libraries/cluster:cluster",
        "//src/libraries/log:log",
        "//src/libraries/giro_validation:giro-validation",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/net",
        "//src/libraries/i18n",
        "//src/libraries/abnamro:abn_amro",
        "//src/libraries/strings:strings",
        "//src/libraries/phone_number_utils:phone_number_utils",
        "//src/libraries/discovered_web_service",
        "//src/libraries/endpoint_configuration",
        "//src/libraries/http:http-annotations",
        "//src/libraries/http_client:http-client",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/api-annotations",
        "//src/libraries/cli_print_utils:cli_print_utils",

        "//third_party:org_springframework_spring_expression",
        "//third_party:com_fasterxml_jackson_dataformat_jackson_dataformat_xml",
        "//third_party:com_github_detro_ghostdriver_phantomjsdriver",
        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_extensions_guice_multibindings",
        "//third_party:com_google_inject_guice",
        "//third_party:com_lambdaworks_scrypt",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:commons_httpclient_commons_httpclient",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:joda_time_joda_time",
        "//third_party:org_apache_commons_commons_math3",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_recipes",
        "//third_party:org_apache_lucene_lucene_core",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_mozilla_rhino",
        "//third_party:org_seleniumhq_selenium_selenium_java",
        "//third_party:org_springframework_spring_core",
        "//third_party:org_springframework_data_spring_data_jpa",
        "//third_party:org_codehaus_jackson_jackson_core_asl",
        "//third_party:com_nimbusds_srp6a",
        "//third_party:org_apache_pdfbox_pdfbox",
        "//third_party:org_apache_pdfbox_fontbox",
        "//third_party:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
        "//third_party:org_assertj_assertj_core",
        "//third_party:net_sourceforge_tess4j",

        "//third_party:net_sourceforge_cssparser_cssparser",

        "//src/cluster-lib:cluster-lib",
    ],
)

java_library(
    name = "agents-lib",
    srcs = glob(["src/agents-lib/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    data = [
        "//tools:phantomjs_mac",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
        "//data:tesseract-training-set",
    ],
    deps = [
        ":common-lib",
        ":encryption-api",
        ":main-api",
        ":aggregation-api",
        ":system-api",
        ":aggregationcontroller-api",

        "//src/cluster-lib:cluster-lib",
        "//src/libraries/uuid:uuid",
        "//src/libraries/http:http-utils",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/discovery:discovery",
        "//src/libraries/metrics:metrics",
        "//src/libraries/jersey_guice:jersey-guice",
        "//src/libraries/jersey_log:jersey-log",
        "//src/libraries/date:date",
        "//src/libraries/cluster:cluster",
        "//src/libraries/log:log",
        "//src/libraries/giro_validation:giro-validation",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/net",
        "//src/libraries/i18n",
        "//src/libraries/abnamro:abn_amro",
        "//src/libraries/strings:strings",
        "//src/libraries/phone_number_utils:phone_number_utils",
        "//src/libraries/discovered_web_service",
        "//src/libraries/endpoint_configuration",
        "//src/libraries/http:http-annotations",
        "//src/libraries/http_client:http-client",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/api-annotations",
        "//src/libraries/cli_print_utils:cli_print_utils",

        "//third_party:org_springframework_spring_expression",
        "//third_party:com_fasterxml_jackson_dataformat_jackson_dataformat_xml",
        "//third_party:com_github_detro_ghostdriver_phantomjsdriver",
        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_extensions_guice_multibindings",
        "//third_party:com_google_inject_guice",
        "//third_party:com_lambdaworks_scrypt",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:commons_httpclient_commons_httpclient",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:joda_time_joda_time",
        "//third_party:org_apache_commons_commons_math3",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_recipes",
        "//third_party:org_apache_lucene_lucene_core",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_mozilla_rhino",
        "//third_party:org_seleniumhq_selenium_selenium_java",
        "//third_party:org_springframework_spring_core",
        "//third_party:org_springframework_data_spring_data_jpa",
        "//third_party:org_codehaus_jackson_jackson_core_asl",
        "//third_party:com_nimbusds_srp6a",
        "//third_party:org_apache_pdfbox_pdfbox",
        "//third_party:org_apache_pdfbox_fontbox",
        "//third_party:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
        "//third_party:org_assertj_assertj_core",
        "//third_party:net_sourceforge_tess4j",
        "//third_party:net_sourceforge_cssparser_cssparser",

    ],
)

java_binary(
    name = "aggregation",
    srcs = glob(["src/aggregation-service/src/main/**/*.java"]),
    data = [
        "etc/abn/development-aggregation-server.yml",
        "etc/kirkby/development-aggregation-server.yml",
        "etc/newport/development-aggregation-server.yml",
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
        ":aggregation-api",
        ":aggregation-lib",
        ":common-lib",
        ":main-api",
        ":agents-lib",
        ":aggregationcontroller-api",

        "//src/libraries/auth:auth",
        "//src/libraries/cluster:cluster",
        "//src/libraries/discovery:discovery",
        "//src/libraries/metrics:metrics",
        "//src/libraries/dropwizard_utils:dropwizard-utils",

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

        "//src/api-annotations-testlib",

        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:org_assertj_assertj_core",
    ],
)

junit_test(
    name = "aggregation-lib-test",
    srcs = glob(["src/aggregation-lib/src/test/**/*.java"]),
    data = [
        "//data:agents",
        "//tools:phantomjs_mac",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
    ],
    runtime_deps = [
        "//third_party:ch_qos_logback_logback_classic",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        ":aggregation-api",
        ":aggregation-lib",
        ":common-lib",
        ":common-lib-testlib",
        ":main-api",
        ":system-api",
        ":agents-lib",

        "//src/cluster-lib:cluster-lib",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/metrics:metrics",
        "//src/libraries/date:date",
        "//src/libraries/giro_validation:giro-validation",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/net",
        "//src/libraries/i18n",
        "//src/libraries/strings:strings",
        "//src/libraries/abnamro:abn_amro",

        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:commons_io_commons_io",
        "//third_party:commons_lang_commons_lang",
        "//third_party:joda_time_joda_time",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_apache_pdfbox_pdfbox",
        "//third_party:pl_pragmatists_junitparams",
        "//third_party:com_nimbusds_srp6a",
        "//third_party:org_bouncycastle_bcprov_jdk15on",
    ],
)


junit_test(
    name = "aggregation-test",
    size = "large",
    srcs = glob(["src/aggregation-tests/src/test/**/*.java"]),
    data = [
        "etc/development.yml",
        "//data:aggregation-test",
        "//data:tesseract-training-set",
    ],
    tags = [
        "external",
        "manual",
    ],
    deps = [
        ":aggregation-api",
        ":aggregation-lib",
        ":common-lib",
        ":common-lib-testlib",
        ":main-api",
        ":system-api",
        ":agents-lib",
        "//src/cluster-lib:cluster-lib",

        "//src/libraries/uuid:uuid",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/metrics:metrics",
        "//src/libraries/date:date",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/i18n",
        "//src/libraries/strings:strings",

        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:io_dropwizard_dropwizard_configuration",
        "//third_party:io_dropwizard_dropwizard_jackson",
        "//third_party:javax_validation_validation_api",
        "//third_party:joda_time_joda_time",
        "//third_party:junit_junit",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:commons_io_commons_io",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_mockito_mockito_core",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
        "//third_party:org_seleniumhq_selenium_selenium_java",
        "//third_party:net_sourceforge_tess4j",
    ],
)

java_library(
     name = "provider-service",
     srcs = glob(["src/provider-service/src/main/**/*.java"]),
     data = [
         "//data",
     ],
     deps = [
         ":common-lib",
          "//src/libraries/auth:auth",
          "//src/libraries/discovery:discovery",
          "//src/libraries/dropwizard_utils:dropwizard-utils",
          "//src/libraries/metrics:metrics",
          "//src/libraries/cluster:cluster",

         "//third_party:com_google_guava_guava",
         "//third_party:com_google_inject_guice",
         "//third_party:com_netflix_governator",
         "//third_party:io_dropwizard_dropwizard_core",
     ],
)

java_binary(
    name = "provider",
    srcs = glob(["src/provider-service/src/main/**/*.java"]),
    data = [
        "etc/development-provider-server.yml",
        "//data",
    ],
    main_class = "se.tink.backend.aggregation.provider.ProviderServiceContainer",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:mysql_mysql_connector_java",
    ],
    deps = [
        ":common-lib",
          "//src/libraries/auth:auth",
          "//src/libraries/discovery:discovery",
          "//src/libraries/dropwizard_utils:dropwizard-utils",
          "//src/libraries/metrics:metrics",
          "//src/libraries/cluster:cluster",

         "//third_party:com_google_guava_guava",
         "//third_party:com_google_inject_guice",
         "//third_party:com_netflix_governator",
         "//third_party:io_dropwizard_dropwizard_core",
    ],
)

genrule(
    name = "renamed-provider-deploy-jar",
    srcs = [":provider_deploy.jar"],
    outs = ["provider-service.jar"],
    cmd = "cp $(location :provider_deploy.jar) \"$(@)\"",
    visibility = ["//visibility:public"],
)
