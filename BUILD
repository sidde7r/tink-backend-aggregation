load("//tools/bzl:junit.bzl", "junit_test")
load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

# TODO: move these into their own component directories when the modules
# have been merged to suitable components.

java_library(
    name = "common-utilities",
    srcs = glob(["src/common-utilities/src/main/**/*.java"]),
    deps = [
        # Log
        "//src/libraries/log:log",

        # Public UUID
        "//src/libraries/uuid:uuid",

        # Request tracing
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:commons_codec_commons_codec",
        "//third_party:org_slf4j_slf4j_api",

        # Serialization utils
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_fasterxml_jackson_dataformat_jackson_dataformat_smile",

        # UUID utils
        "//third_party:com_datastax_cassandra_cassandra_driver_core",

        # Dependency injection
        "//third_party:com_google_inject_guice",
    ],
    visibility = ["//visibility:public"],
)


java_library(
    name = "main-api-testlib",
    srcs = glob(["src/main-api-testlib/src/main/**/*.java"]),
    deps = [
        ":main-api",
        ":common-utilities",

        "//src/libraries/uuid:uuid",
        "//src/libraries/date:date",

        "//third_party:com_google_guava_guava",
    ],
    visibility = ["//visibility:public"],
)

java_library(
    name = "connector-api",
    srcs = glob(["src/connector-api/src/main/**/*.java"]),
    deps = [
        ":common-utilities",
        "@tink_backend_encryption//:encryption-api",
        ":main-api",

        "//src/libraries/http:http-annotations",
        "//src/libraries/i18n",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/http_client:http-client",
        "//src/api-annotations",

        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:eu_geekplace_javapinning_java_pinning_jar",
        "//third_party:io_swagger_swagger_annotations",
        "//third_party:javax_validation_validation_api",
    ],
)


java_library(
    name = "connector-lib",
    srcs = glob(["src/connector-lib/src/main/**/*.java"]),
    deps = [
        ":aggregation-api",
        ":common-lib",
        ":common-utilities",
        ":connector-api",
        "@tink_backend_encryption//:encryption-api",
        ":main-api",
        ":system-api",
        "//src/categorization-api",

        "//src/libraries/http:http-annotations",
        "//src/libraries/http:http-utils",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/auth:auth",
        "//src/libraries/discovery:discovery",
        "//src/libraries/metrics:metrics",
        "//src/libraries/jersey_guice:jersey-guice",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/jersey_log:jersey-log",
        "//src/libraries/http_client:http-client",
        "//src/libraries/date:date",
        "//src/libraries/cluster:cluster",
        "//src/libraries/uuid",
        "//src/libraries/i18n",
        "//src/libraries/abnamro:abn_amro",
        "//src/api-annotations",

        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_lambdaworks_scrypt",
        "//third_party:com_netflix_governator",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_reactivex_rxjava",
        "//third_party:io_swagger_swagger_annotations",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_recipes",
        "//third_party:org_modelmapper_modelmapper",
        "//third_party:org_assertj_assertj_core",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4"
    ],
)


java_library(
    name = "aggregation-api",
    srcs = glob(["src/aggregation-api/src/main/**/*.java"]),
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
    name = "insights-api",
    srcs = glob(["src/insights-api/src/main/**/*.java"]),
    deps = [
        ":main-api",
        "//src/api-annotations",
        "//src/libraries/auth",
        "@javax_inject_javax_inject//jar",
        "//src/libraries/http:http-annotations",
        "//src/libraries/http:http-utils",
        "//src/libraries/http_client:http-client",
        "//third_party:com_sun_jersey_jersey_core",
        "//src/libraries/jersey_utils:jersey-utils",

    ],
)

java_library(
    name = "gdpr-export-api",
    srcs = glob(["src/gdpr-export-api/src/main/**/*.java"]),
    deps = [
        ":main-api",
        "//src/api-annotations",
        "//src/libraries/auth",
        "@javax_inject_javax_inject//jar",
        "//src/libraries/http:http-annotations",
        "//src/libraries/http_client:http-client",
        "//src/libraries/jersey_utils:jersey-utils",
        "//third_party:com_sun_jersey_jersey_core",


    ],
)

java_proto_library(
    name = "firehose-v1-java-pb",
    protos = [":firehose-v1-pb"],
)

filegroup(
    name = "firehose-v1-pb",
    srcs = glob(["src/firehose-v1-lib/src/main/proto/*.proto"]),
)

java_library(
    name = "firehose-v1-lib",
    srcs = glob(["src/firehose-v1-lib/src/main/**/*.java"]),
    deps = [
        ":main-api",
        ":queue-lib",
        ":common-lib",
        ":common-utilities",
        ":firehose-v1-java-pb",

        "//src/libraries/uuid:uuid",
        "//src/libraries/date:date",
        "//src/libraries/serialization_utils:serialization-utils",

        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:org_modelmapper_modelmapper",
    ],
)

java_library(
    name = "product-executor-lib",
    srcs = glob(["src/fs-product-execution-lib/src/main/**/*.java"]),
    data = [
    ],
    deps = [
        "//:main-api",
        ":common-lib",
        ":system-api",
        ":product-executor-api",
        "//src/categorization-api",
        "//src/libraries/auth",
        "//src/libraries/cluster:cluster",
        "//src/libraries/date:date",
        "//third_party:joda_time_joda_time",
        "//src/libraries/discovery",
        "//src/libraries/endpoint_configuration:endpoint_configuration",
        "//src/libraries/jersey_guice:jersey-guice",
        "//src/libraries/log",
        "//src/libraries/metrics",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/uuid",
        "//src/libraries/identity",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_apache_commons_commons_math",
        "//third_party:net_spy_spymemcached",
        "//third_party:org_hibernate_hibernate_core",
        "//third_party:org_rythmengine_rythm_engine",
        "//src/libraries/protobuf_serialization_utils:protobuf-serialization-utils",

        "//src/libraries/net",
        "//src/libraries/i18n",
        "//third_party:org_bouncycastle_bcprov_jdk15on",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//src/libraries/generic_application:generic-application",
        "//third_party:com_sun_jersey_jersey_client",
        "//src/libraries/dropwizard_utils:dropwizard-utils",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_mockito_mockito_core",
    ],
)


java_library(
    name = "product-executor-api",
    srcs = glob(["src/fs-product-execution-api/src/main/**/*.java"]),
    deps = [
        "//src/api-annotations",
        "//:main-api",
        "@javax_inject_javax_inject//jar",
        "//src/libraries/http:http-annotations",
        "//src/libraries/generic_application:generic-application",

        "//src/libraries/http:http-utils",
        "//src/libraries/http_client:http-client",
        "//third_party:com_sun_jersey_jersey_core",
        "//src/libraries/jersey_utils:jersey-utils",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "@org_pubref_rules_protobuf//java:grpc_compiletime_deps",
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
        ":common-utilities",
        ":tink-oauth-grpc",
        ":tink-oauth-grpc_compile_imports",
        ":firehose-v1-java-pb",

        "//src/consent-lib",
        "//src/sms-otp-lib",

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
        "//third_party:org_springframework_data_spring_data_cassandra",
        "//third_party:org_xerial_snappy_snappy_java",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:commons_codec_commons_codec",
        "//third_party:com_lambdaworks_scrypt",
        "//third_party:org_json_json",
    ],
    visibility = ["//visibility:public"],
)

java_library(
    name = "queue-lib",
    srcs = glob(["src/queue-lib/src/main/**/*.java"]),
    deps = [
        ":common-lib",

        "//src/libraries/metrics:metrics",

        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_google_protobuf_protobuf_java",
        "//third_party:org_apache_kafka_kafka_clients",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:com_sproutsocial_nsqj_j",
    ],
)

java_library(
    name = "common-lib",
    srcs = glob(["src/common-lib/src/main/**/*.java"]),
    deps = [
        ":aggregation-api",
        "//src/categorization-api",
        "//src/consent-lib",
        "//src/sms-otp-lib",
        "//src/sms-gateway-lib",
        ":common-utilities",
        ":connector-api",
        "@tink_backend_encryption//:encryption-api",
        ":firehose-v1-java-pb",
        ":main-api",
        ":system-api",
        ":insights-api",
        ":gdpr-export-api",
        ":product-executor-api",

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
        "//third_party:org_springframework_data_spring_data_cassandra",
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
        ":common-utilities",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:pl_pragmatists_junitparams",
    ],
    deps = [
        ":common-lib",
        ":common-utilities",
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
        "//third_party:org_springframework_data_spring_data_cassandra",
    ],
)


java_library(
    name = "system-lib",
    srcs = glob(["src/system-lib/src/main/**/*.java"]),
    deps = [
        ":aggregation-api",
        "//src/categorization-api",
        "//src/categorization-lib",
        "//src/consent-lib",
        "//src/sms-otp-lib",
        ":common-lib",
        ":common-utilities",
        ":connector-api",
        "@tink_backend_encryption//:encryption-api",
        ":firehose-v1-java-pb",
        ":firehose-v1-lib",
        ":main-api",
        ":queue-lib",
        ":system-api",
        "//:insights-api",
        ":product-executor-api",

        "//src/libraries/abnamro:abn_amro",
        "//src/libraries/uuid:uuid",
        "//src/libraries/http:http-utils",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/auth:auth",
        "//src/libraries/discovery:discovery",
        "//src/libraries/metrics:metrics",
        "//src/libraries/jersey_guice:jersey-guice",
        "//src/libraries/jersey_log:jersey-log",
        "//src/libraries/dropwizard_utils:dropwizard-utils",
        "//src/libraries/date:date",
        "//src/libraries/cluster:cluster",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/cassandra-interfaces",
        "//src/libraries/log:log",
        "//src/libraries/net",
        "//src/libraries/i18n",
        "//src/libraries/identity",
        "//src/libraries/http_client:http-client",

        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:com_google_inject_guice",
        "//third_party:com_jcraft_jsch",
        "//third_party:com_maxmind_geoip2_geoip2",
        "//third_party:com_restfb_restfb",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:de_grundid_opendatalab_geojson_jackson",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_lifecycle",
        "//third_party:io_reactivex_rxjava",
        "//third_party:joda_time_joda_time",
        "//third_party:net_sourceforge_jexcelapi_jxl",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_apache_commons_commons_csv",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_recipes",
        # TODO: Check if still used.
        "//third_party:org_apache_hadoop_hadoop_core",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_apache_mahout_mahout_core",
        "//third_party:org_elasticsearch_elasticsearch",
        "//third_party:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
        "//third_party:org_json_json",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_modelmapper_modelmapper",
        "//third_party:org_quartz_scheduler_quartz",
        "//third_party:org_springframework_data_spring_cql",
        "//third_party:org_springframework_data_spring_data_cassandra",
        "//third_party:org_springframework_data_spring_data_commons",
        "//third_party:org_springframework_spring_core",
        "//third_party:org_apache_pdfbox_pdfbox",
        "//third_party:org_apache_pdfbox_fontbox",
        "//third_party:org_assertj_assertj_core",
        "//third_party:com_clearspring_analytics_stream",
        "//third_party:org_freemarker",
        "//third_party:com_lambdaworks_scrypt",
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
         "@tink_backend_encryption//:encryption-lib",
         ":main-api",
         ":system-api",
         ":system-lib",
         ":firehose-v1-lib",

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
    name = "aggregation-controller-api",
    srcs = glob(["src/aggregation-controller-api/src/main/**/*.java"]),
    data = [
        "etc/development-minikube-aggregation-controller-server.yml",
        "//data",
    ],
    deps = [
        ":common-lib",
        ":aggregation-api",
        ":main-api",
        ":firehose-v1-java-pb",

        "//src/libraries/http:http-annotations",
        "//src/api-annotations",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/http_client:http-client",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/discovery:discovery",
        "//src/libraries/discovered_web_service:discovered_web_service",
        "//src/libraries/endpoint_configuration:endpoint_configuration",
        "//src/libraries/jersey_guice:jersey-guice",
        "//src/libraries/generic_application:generic-application",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "//src/libraries/date",

        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_google_inject_guice",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_extensions_guice_multibindings",
        "//third_party:com_google_guava_guava",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:commons_codec_commons_codec",
        "//third_party:commons_beanutils_commons_beanutils",
        "//third_party:org_hibernate_hibernate_annotations",
        "//third_party:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_modelmapper_modelmapper",
        "//third_party:joda_time_joda_time",
    ],
)

java_library(
    name = "aggregation-controller-lib",
    srcs = glob(["src/aggregation-controller-lib/src/main/**/*.java"]),
    data = [
        "etc/development-minikube-aggregation-controller-server.yml",
        "//data",
    ],
    deps = [
        ":common-lib",
        ":main-api",
        ":system-api",
        ":aggregation-api",
        ":aggregation-controller-api",

        "//src/libraries/jersey_log:jersey-log",
        "//src/libraries/jersey_guice:jersey-guice",
        "//src/libraries/metrics:metrics",
        "//src/libraries/http:http-annotations",
        "//src/api-annotations",
        "//src/libraries/jersey_utils:jersey-utils",
        "//src/libraries/http_client:http-client",
        "//src/libraries/discovery:discovery",
        "//src/libraries/discovered_web_service:discovered_web_service",
        "//src/libraries/endpoint_configuration:endpoint_configuration",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/log:log",
        "//src/libraries/http:http-utils",

        "//third_party:com_google_inject_guice",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_extensions_guice_multibindings",
        "//third_party:com_google_guava_guava",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:com_netflix_governator",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:com_sun_jersey_jersey_client",
    ],
)

java_library(
    name = "webhook-lib",
    srcs = glob(["src/webhook-lib/src/main/**/*.java"]),
    data = [
        "etc/development-webhook-server.yml",
        "//data",
    ],
    deps = [
        ":common-lib",
        ":firehose-v1-java-pb",
        ":firehose-v1-lib",
        ":main-api",
        ":queue-lib",

        "//src/libraries/dropwizard_utils:dropwizard-utils",
        "//src/libraries/http:http-annotations",
        "//src/libraries/jersey_guice:jersey-guice",
        "//src/libraries/metrics:metrics",
        "//src/libraries/net",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/api-annotations",

        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_netflix_governator",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:io_dropwizard_dropwizard_core",
    ],
)

java_proto_library(
    name = "aggregation-grpc",
    protos = glob(["src/aggregation-grpc/src/main/proto/**/*.proto"]),
    with_grpc = True,
)

java_proto_library(
    name = "tink-oauth-grpc",
    protos = glob(["tink-oauth-grpc/*.proto"]),
    with_grpc = True,
)

java_library(
    name = "oauth-grpc-client",
    srcs = glob(["src/oauth-grpc-client/src/main/**/*.java"]),
    runtime_deps = [
        "//third_party:io_grpc_grpc_core",
    ],
    deps = [
        ":tink-oauth-grpc",
        ":tink-oauth-grpc_compile_imports",
        ":common-lib",

        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_lifecycle",

        "//src/libraries/log:log",
        "//src/libraries/endpoint_configuration:endpoint_configuration",
    ]
)

java_library(
    name = "aggregation-grpc-client",
    srcs = glob(["src/aggregation-grpc-client/src/main/**/*.java"]),
    runtime_deps = [
        "//third_party:com_google_instrumentation_instrumentation_api",
        "//third_party:io_grpc_grpc_context",
        "//third_party:io_grpc_grpc_core",
        "//third_party:io_grpc_grpc_netty",
        "//third_party:io_netty_netty_codec_http2",
        "//third_party:io_netty_netty_handler",
        "//third_party:io_netty_netty_handler_proxy",
        "//third_party:io_netty_netty_transport",
    ],
    deps = [
        ":aggregation-grpc",
        ":aggregation-grpc_compile_imports",
        ":main-api",
        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_x_discovery",
        # Neede solely to avoid compilation warning about not found class
        # probably caused by the dependency on :main-api
        "//third_party:org_codehaus_jackson_jackson_core_asl",
    ],
)

junit_test(
    name = "aggregation-grpc-client-test",
    srcs = glob(["src/aggregation-grpc-client/src/test/**/*.java"]),
    runtime_deps = [
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        ":aggregation-grpc",
        ":aggregation-grpc-client",
        ":aggregation-grpc_compile_imports",
        ":aggregation-lib",
        ":main-api",
        "//third_party:junit_junit",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:org_codehaus_jackson_jackson_core_asl",
        "//third_party:org_mockito_mockito_core",
    ],
)

java_library(
    name = "aggregation-lib",
    srcs = glob(["src/aggregation-lib/src/main/**/*.java"]),
    runtime_deps = [
        # gRPC
        "//third_party:io_grpc_grpc_netty",
        "//third_party:io_grpc_grpc_context",
        "//third_party:com_google_instrumentation_instrumentation_api",
    ],
    data = [
        "//tools:phantomjs_mac",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
    ],
    deps = [
        ":aggregation-api",
        ":aggregation-grpc",
        ":aggregation-grpc_compile_imports",
        ":common-lib",
        ":common-utilities",
        "@tink_backend_encryption//:encryption-api",
        ":main-api",
        ":system-api",

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

java_proto_library(
    name = "tink-grpc-v1-api-java",
    protos = [":tink-grpc-v1-api"],
    visibility = ["//visibility:public"],
    with_grpc = True,
)

filegroup(
    name = "tink-grpc-v1-api",
    srcs = glob([
        "proto/*.proto",
        "google/**/*.proto",
    ]),
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
        ":aggregation-grpc",
        ":aggregation-grpc_compile_imports",
        ":aggregation-lib",
        ":common-lib",
        ":common-lib-testlib",
        ":common-utilities",
        ":main-api",
        ":system-api",

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
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_mockito_mockito_core",
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
)
