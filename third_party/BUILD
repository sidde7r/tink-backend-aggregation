load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "asm_asm",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:asm_asm",
    ],
)

java_library(
    name = "c3p0_c3p0",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:c3p0_c3p0",
    ],
)

java_library(
    name = "ch_qos_logback_logback_classic",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:ch_qos_logback_logback_classic",
    ],
    runtime_deps = [
        "@aggregation//:ch_qos_logback_logback_core",
        "@aggregation//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "ch_qos_logback_contrib_logback_jackson",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:ch_qos_logback_contrib_logback_jackson",
    ],
)

java_library(
    name = "ch_qos_logback_contrib_logback_json_classic",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:ch_qos_logback_contrib_logback_json_classic",
    ],
    runtime_deps = [
        "@aggregation//:ch_qos_logback_contrib_logback_jackson",
        "@aggregation//:ch_qos_logback_contrib_logback_json_core",
    ],
)

java_library(
    name = "ch_qos_logback_contrib_logback_json_core",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:ch_qos_logback_contrib_logback_json_core",
    ],
)

java_library(
    name = "com_amazonaws_aws_java_sdk_s3",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:com_amazonaws_aws_java_sdk_s3",
    ],
    runtime_deps = [
        "//third_party/aws_java_sdk_core",
        "@aggregation_temp//:com_amazonaws_aws_java_sdk_code_generator",
        "@aggregation_temp//:com_amazonaws_aws_java_sdk_kms",
        "@aggregation_temp//:com_amazonaws_jmespath_java",
    ],
)

java_library(
    name = "com_fasterxml_jackson_core_jackson_annotations",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
    ],
)

java_library(
    name = "com_fasterxml_jackson_core_jackson_core",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
    ],
)

java_library(
    name = "com_fasterxml_jackson_core_jackson_databind",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
    runtime_deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
    ],
)

java_library(
    name = "com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
    visibility = ["//visibility:public"],
    exports = [
        # *base apparently needs to exported alongside *provider
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_base",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
    ],
    runtime_deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_module_jackson_module_jaxb_annotations",
    ],
)

java_library(
    name = "com_fasterxml_jackson_dataformat_jackson_dataformat_smile",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_smile",
    ],
    runtime_deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
    ],
)

java_library(
    name = "com_fasterxml_jackson_dataformat_jackson_dataformat_xml",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_xml",
    ],
    runtime_deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_module_jackson_module_jaxb_annotations",
        "@aggregation//:javax_xml_stream_stax_api",
        "@aggregation//:org_codehaus_woodstox_stax2_api",
    ],
)

java_library(
    # TODO check is this used?
    name = "com_fasterxml_jackson_datatype_jackson_datatype_joda",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_joda",
    ],
    runtime_deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
    ],
)

java_library(
    name = "com_github_rholder_guava_retrying",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_github_rholder_guava_retrying",
    ],
    runtime_deps = [
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_guava_guava",
    ],
)

java_library(
    name = "com_github_tomakehurst_wiremock",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:com_github_tomakehurst_wiremock_standalone",
    ],
    runtime_deps = [
        "@aggregation//:asm_asm",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_flipkart_zjsonpatch_zjsonpatch",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_jayway_jsonpath_json_path",
        "@aggregation//:commons_codec_commons_codec",
        "@aggregation//:commons_logging_commons_logging",
        "@aggregation//:javax_servlet_javax_servlet_api",
        "@aggregation//:junit_junit",
        "@aggregation//:net_minidev_asm",
        "@aggregation//:net_minidev_json_smart",
        "@aggregation//:net_sf_jopt_simple_jopt_simple",
        "@aggregation//:org_apache_commons_commons_collections4",
        "@aggregation//:org_apache_commons_commons_lang3",
        "@aggregation//:org_apache_httpcomponents_httpclient",
        "@aggregation//:org_apache_httpcomponents_httpcore",
        "@aggregation//:org_eclipse_jetty_jetty_continuation",
        "@aggregation//:org_eclipse_jetty_jetty_http",
        "@aggregation//:org_eclipse_jetty_jetty_io",
        "@aggregation//:org_eclipse_jetty_jetty_security",
        "@aggregation//:org_eclipse_jetty_jetty_server",
        "@aggregation//:org_eclipse_jetty_jetty_servlet",
        "@aggregation//:org_eclipse_jetty_jetty_servlets",
        "@aggregation//:org_eclipse_jetty_jetty_util",
        "@aggregation//:org_slf4j_slf4j_api",
        "@aggregation//:org_xmlunit_xmlunit_core",
        "@aggregation//:org_xmlunit_xmlunit_legacy",
    ],
)

java_library(
    name = "org_apache_commons_commons_collections4",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_apache_commons_commons_collections4",
    ],
)

java_library(
    name = "com_google_code_findbugs_jsr305",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_code_findbugs_jsr305",
    ],
)

java_library(
    name = "com_google_code_gson_gson",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_code_gson_gson",
    ],
)

java_library(
    name = "com_google_guava_guava",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_guava_guava",
    ],
)

java_library(
    name = "com_google_http_client_google_http_client",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_http_client_google_http_client",
    ],
    runtime_deps = [
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:org_apache_httpcomponents_httpclient",
    ],
)

java_library(
    name = "com_google_inject_extensions_guice_multibindings",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_inject_extensions_guice_multibindings",
    ],
    runtime_deps = [
        "@aggregation//:com_google_inject_guice",
    ],
)

java_library(
    name = "com_google_inject_guice",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_inject_guice",
    ],
    runtime_deps = [
        "@aggregation//:aopalliance_aopalliance",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:javax_inject_javax_inject",
    ],
)

java_library(
    name = "javax_inject_javax_inject",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:javax_inject_javax_inject",
    ],
)

java_library(
    name = "com_google_protobuf_protobuf_java",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_protobuf_protobuf_java",
    ],
)

java_library(
    name = "com_netflix_governator",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_netflix_governator_governator",
    ],
    runtime_deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_inject_extensions_guice_assistedinject",
        "@aggregation//:com_google_inject_extensions_guice_grapher",
        "@aggregation//:com_google_inject_extensions_guice_multibindings",
        "@aggregation//:com_google_inject_guice",
        "@aggregation//:com_netflix_governator_governator_api",
        "@aggregation//:com_netflix_governator_governator_core",
        "@aggregation//:javax_inject_javax_inject",
    ],
)

java_library(
    name = "com_google_instrumentation_instrumentation_api",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_instrumentation_instrumentation_api",
    ],
    runtime_deps = [
        "@aggregation//:com_google_code_findbugs_jsr305",
    ],
)

java_library(
    name = "com_googlecode_gettext_commons_gettext_commons",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_googlecode_gettext_commons_gettext_commons",
    ],
)

java_library(
    name = "com_lambdaworks_scrypt",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_lambdaworks_scrypt",
    ],
)

java_library(
    name = "com_sun_jersey_contribs_jersey_apache_client4",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_sun_jersey_contribs_jersey_apache_client4",
    ],
    runtime_deps = [
        "@aggregation//:com_sun_jersey_jersey_client",
    ],
)

java_library(
    name = "com_sun_jersey_jersey_client",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_sun_jersey_jersey_client",
    ],
    runtime_deps = [
        "@aggregation//:com_sun_jersey_jersey_core",
    ],
)

java_library(
    name = "com_sun_jersey_jersey_core",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_sun_jersey_jersey_core",
    ],
)

java_library(
    name = "com_sun_jersey_jersey_server",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_sun_jersey_jersey_server",
    ],
    runtime_deps = [
        "@aggregation//:com_sun_jersey_jersey_core",
    ],
)

java_library(
    name = "com_yubico_yubico_validation_client2",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_yubico_yubico_validation_client2",
    ],
    runtime_deps = [
        "@aggregation//:commons_codec_commons_codec",
        "@aggregation//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "commons_codec_commons_codec",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:commons_codec_commons_codec",
    ],
)

java_library(
    name = "commons_httpclient_commons_httpclient",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:commons_httpclient_commons_httpclient",
    ],
)

java_library(
    name = "commons_io_commons_io",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:commons_io_commons_io",
    ],
)

java_library(
    name = "de_jollyday_jollyday",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:de_jollyday_jollyday",
    ],
    runtime_deps = [
        "@aggregation//:javax_xml_bind_jaxb_api",
        "@aggregation//:joda_time_joda_time",
    ],
)

java_library(
    name = "eu_geekplace_javapinning_java_pinning_jar",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:eu_geekplace_javapinning_java_pinning_jar",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_client",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:io_dropwizard_dropwizard_client",
    ],
    runtime_deps = [
        "@aggregation//:com_codahale_metrics_metrics_core",
        "@aggregation//:com_codahale_metrics_metrics_httpclient",
        "@aggregation//:com_sun_jersey_contribs_jersey_apache_client4",
        "@aggregation//:com_sun_jersey_jersey_client",
        "@aggregation//:commons_codec_commons_codec",
        "@aggregation//:org_apache_httpcomponents_httpclient",
        "@aggregation//:org_apache_httpcomponents_httpcore",
        "@aggregation_temp//:io_dropwizard_dropwizard_core",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_configuration",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_dropwizard_dropwizard_configuration",
    ],
    runtime_deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_yaml",
        "@aggregation//:commons_lang_commons_lang",
        "@aggregation//:io_dropwizard_dropwizard_jackson",
        "@aggregation//:io_dropwizard_dropwizard_validation",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_core",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:io_dropwizard_dropwizard_core",
    ],
    runtime_deps = [
        ":io_dropwizard_dropwizard_jersey",
        "@aggregation//:ch_qos_logback_logback_classic",
        "@aggregation//:ch_qos_logback_logback_core",
        "@aggregation//:com_codahale_metrics_metrics_annotation",
        "@aggregation//:com_codahale_metrics_metrics_core",
        "@aggregation//:com_codahale_metrics_metrics_healthchecks",
        "@aggregation//:com_codahale_metrics_metrics_jersey",
        "@aggregation//:com_codahale_metrics_metrics_jetty9",
        "@aggregation//:com_codahale_metrics_metrics_json",
        "@aggregation//:com_codahale_metrics_metrics_jvm",
        "@aggregation//:com_codahale_metrics_metrics_logback",
        "@aggregation//:com_codahale_metrics_metrics_servlets",
        "@aggregation//:com_fasterxml_classmate",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_yaml",
        "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_guava",
        "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_joda",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_base",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
        "@aggregation//:com_fasterxml_jackson_module_jackson_module_afterburner",
        "@aggregation//:com_fasterxml_jackson_module_jackson_module_jaxb_annotations",
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_sun_jersey_jersey_core",
        "@aggregation//:com_sun_jersey_jersey_server",
        "@aggregation//:com_sun_jersey_jersey_servlet",
        "@aggregation//:commons_lang_commons_lang",
        "@aggregation//:io_dropwizard_dropwizard_configuration",
        "@aggregation//:io_dropwizard_dropwizard_jackson",
        "@aggregation//:io_dropwizard_dropwizard_jetty",
        "@aggregation//:io_dropwizard_dropwizard_lifecycle",
        "@aggregation//:io_dropwizard_dropwizard_logging",
        "@aggregation//:io_dropwizard_dropwizard_metrics",
        "@aggregation//:io_dropwizard_dropwizard_servlets",
        "@aggregation//:io_dropwizard_dropwizard_util",
        "@aggregation//:io_dropwizard_dropwizard_validation",
        "@aggregation//:javax_el_javax_el_api",
        "@aggregation//:javax_validation_validation_api",
        "@aggregation//:net_sourceforge_argparse4j_argparse4j",
        "@aggregation//:org_eclipse_jetty_jetty_continuation",
        "@aggregation//:org_eclipse_jetty_jetty_http",
        "@aggregation//:org_eclipse_jetty_jetty_io",
        "@aggregation//:org_eclipse_jetty_jetty_security",
        "@aggregation//:org_eclipse_jetty_jetty_server",
        "@aggregation//:org_eclipse_jetty_jetty_servlet",
        "@aggregation//:org_eclipse_jetty_jetty_servlets",
        "@aggregation//:org_eclipse_jetty_jetty_util",
        "@aggregation//:org_eclipse_jetty_orbit_javax_servlet",
        "@aggregation//:org_glassfish_web_javax_el",
        "@aggregation//:org_hibernate_hibernate_validator",
        "@aggregation//:org_jboss_logging_jboss_logging",
        "@aggregation//:org_slf4j_jcl_over_slf4j",
        "@aggregation//:org_slf4j_jul_to_slf4j",
        "@aggregation//:org_slf4j_slf4j_api",
        "@aggregation//:org_yaml_snakeyaml",
        "@aggregation_temp//:org_eclipse_jetty_toolchain_setuid_jetty_setuid_java",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_servlets",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_dropwizard_dropwizard_servlets",
    ],
    runtime_deps = [
        "@aggregation//:ch_qos_logback_logback_classic",
        "@aggregation//:com_codahale_metrics_metrics_core",
        "@aggregation//:org_eclipse_jetty_jetty_servlet",
        "@aggregation//:org_eclipse_jetty_orbit_javax_servlet",
    ],
)

java_library(
    name = "com_codahale_metrics_metrics_healthchecks",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_codahale_metrics_metrics_healthchecks",
    ],
)

java_library(
    name = "net_sourceforge_argparse4j_argparse4j",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:net_sourceforge_argparse4j_argparse4j",
    ],
)

java_import(
    name = "io_dropwizard_dropwizard_jersey",
    jars = [
        # This should be a java_librar w/ exports instead, see:
        # https://github.com/bazelbuild/intellij/issues/66
        "@dropwizard_jersey//:libdropwizard-jersey.jar",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_codahale_metrics_metrics_annotation",
        "@aggregation//:com_codahale_metrics_metrics_core",
        "@aggregation//:com_codahale_metrics_metrics_jersey",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_base",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
        "@aggregation//:com_fasterxml_jackson_module_jackson_module_jaxb_annotations",
        "@aggregation//:com_sun_jersey_jersey_core",
        "@aggregation//:com_sun_jersey_jersey_server",
        "@aggregation//:com_sun_jersey_jersey_servlet",
        "@aggregation//:io_dropwizard_dropwizard_jackson",
        "@aggregation//:io_dropwizard_dropwizard_logging",
        "@aggregation//:io_dropwizard_dropwizard_validation",
        "@aggregation//:org_eclipse_jetty_orbit_javax_servlet",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_jetty",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_dropwizard_dropwizard_jetty",
    ],
    runtime_deps = [
        ":io_dropwizard_dropwizard_jersey",
        "@aggregation//:ch_qos_logback_logback_classic",
        "@aggregation//:ch_qos_logback_logback_core",
        "@aggregation//:com_codahale_metrics_metrics_annotation",
        "@aggregation//:com_codahale_metrics_metrics_core",
        "@aggregation//:com_codahale_metrics_metrics_healthchecks",
        "@aggregation//:com_codahale_metrics_metrics_jersey",
        "@aggregation//:com_codahale_metrics_metrics_jetty9",
        "@aggregation//:com_codahale_metrics_metrics_json",
        "@aggregation//:com_codahale_metrics_metrics_jvm",
        "@aggregation//:com_codahale_metrics_metrics_logback",
        "@aggregation//:com_codahale_metrics_metrics_servlets",
        "@aggregation//:com_fasterxml_classmate",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_yaml",
        "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_guava",
        "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_joda",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_base",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
        "@aggregation//:com_fasterxml_jackson_module_jackson_module_afterburner",
        "@aggregation//:com_fasterxml_jackson_module_jackson_module_jaxb_annotations",
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_sun_jersey_jersey_core",
        "@aggregation//:com_sun_jersey_jersey_server",
        "@aggregation//:com_sun_jersey_jersey_servlet",
        "@aggregation//:commons_lang_commons_lang",
        "@aggregation//:io_dropwizard_dropwizard_configuration",
        "@aggregation//:io_dropwizard_dropwizard_jackson",
        "@aggregation//:io_dropwizard_dropwizard_jetty",
        "@aggregation//:io_dropwizard_dropwizard_lifecycle",
        "@aggregation//:io_dropwizard_dropwizard_logging",
        "@aggregation//:io_dropwizard_dropwizard_metrics",
        "@aggregation//:io_dropwizard_dropwizard_servlets",
        "@aggregation//:io_dropwizard_dropwizard_util",
        "@aggregation//:io_dropwizard_dropwizard_validation",
        "@aggregation//:javax_el_javax_el_api",
        "@aggregation//:javax_validation_validation_api",
        "@aggregation//:net_sourceforge_argparse4j_argparse4j",
        "@aggregation//:org_eclipse_jetty_jetty_continuation",
        "@aggregation//:org_eclipse_jetty_jetty_http",
        "@aggregation//:org_eclipse_jetty_jetty_io",
        "@aggregation//:org_eclipse_jetty_jetty_security",
        "@aggregation//:org_eclipse_jetty_jetty_server",
        "@aggregation//:org_eclipse_jetty_jetty_servlet",
        "@aggregation//:org_eclipse_jetty_jetty_servlets",
        "@aggregation//:org_eclipse_jetty_jetty_util",
        "@aggregation//:org_eclipse_jetty_orbit_javax_servlet",
        "@aggregation//:org_glassfish_web_javax_el",
        "@aggregation//:org_hibernate_hibernate_validator",
        "@aggregation//:org_jboss_logging_jboss_logging",
        "@aggregation//:org_slf4j_jcl_over_slf4j",
        "@aggregation//:org_slf4j_jul_to_slf4j",
        "@aggregation//:org_slf4j_slf4j_api",
        "@aggregation//:org_yaml_snakeyaml",
        "@aggregation_temp//:org_eclipse_jetty_toolchain_setuid_jetty_setuid_java",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_lifecycle",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_dropwizard_dropwizard_lifecycle",
    ],
    runtime_deps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:io_dropwizard_dropwizard_util",
        "@aggregation//:org_eclipse_jetty_jetty_http",
        "@aggregation//:org_eclipse_jetty_jetty_io",
        "@aggregation//:org_eclipse_jetty_jetty_server",
        "@aggregation//:org_eclipse_jetty_jetty_util",
        "@aggregation//:org_eclipse_jetty_orbit_javax_servlet",
        "@aggregation//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_logging",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:ch_qos_logback_logback_classic",
        "@aggregation//:ch_qos_logback_logback_core",
        "@aggregation//:com_codahale_metrics_metrics_core",
        "@aggregation//:com_codahale_metrics_metrics_logback",
        "@aggregation//:io_dropwizard_dropwizard_jackson",
        "@aggregation//:io_dropwizard_dropwizard_logging",
        "@aggregation//:io_dropwizard_dropwizard_validation",
        "@aggregation//:org_eclipse_jetty_jetty_util",
        "@aggregation//:org_slf4j_jcl_over_slf4j",
        "@aggregation//:org_slf4j_jul_to_slf4j",
        "@aggregation//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_metrics",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_codahale_metrics_metrics_core",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:io_dropwizard_dropwizard_jackson",
        "@aggregation//:io_dropwizard_dropwizard_lifecycle",
        "@aggregation//:io_dropwizard_dropwizard_metrics",
        "@aggregation//:io_dropwizard_dropwizard_util",
        "@aggregation//:io_dropwizard_dropwizard_validation",
        "@aggregation//:org_eclipse_jetty_jetty_http",
        "@aggregation//:org_eclipse_jetty_jetty_io",
        "@aggregation//:org_eclipse_jetty_jetty_server",
        "@aggregation//:org_eclipse_jetty_jetty_util",
        "@aggregation//:org_eclipse_jetty_orbit_javax_servlet",
        "@aggregation//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "io_grpc_grpc_core",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:io_grpc_grpc_api",
        "@aggregation_temp//:io_grpc_grpc_core",
    ],
    runtime_deps = [
        "@aggregation//:io_perfmark_perfmark_api",
    ],
)

java_library(
    # is it necessary ?
    name = "io_grpc_grpc_protobuf_lite",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:io_opencensus_opencensus_api",
        "@aggregation_temp//:io_grpc_grpc_api",
        "@aggregation_temp//:io_grpc_grpc_core",
        "@aggregation_temp//:io_grpc_grpc_protobuf_lite",
        "@aggregation_temp//:io_grpc_grpc_protobuf_nano",
        "@aggregation_temp//:io_opencensus_opencensus_contrib_grpc_metrics",
    ],
)

java_library(
    name = "io_netty_netty_codec",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_netty_netty_buffer",
        "@aggregation//:io_netty_netty_codec",
        "@aggregation//:io_netty_netty_common",
        "@aggregation//:io_netty_netty_resolver",
        "@aggregation//:io_netty_netty_transport",
    ],
)

java_library(
    name = "io_netty_netty_codec_http",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_netty_netty_buffer",
        "@aggregation//:io_netty_netty_codec",
        "@aggregation//:io_netty_netty_codec_http",
        "@aggregation//:io_netty_netty_common",
        "@aggregation//:io_netty_netty_resolver",
        "@aggregation//:io_netty_netty_transport",
    ],
)

java_library(
    name = "io_netty_netty_all",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_netty_netty_buffer",
        "@aggregation//:io_netty_netty_codec",
        "@aggregation//:io_netty_netty_codec_dns",
        "@aggregation//:io_netty_netty_codec_http",
        "@aggregation//:io_netty_netty_codec_http2",
        "@aggregation//:io_netty_netty_codec_socks",
        "@aggregation//:io_netty_netty_common",
        "@aggregation//:io_netty_netty_handler",
        "@aggregation//:io_netty_netty_handler_proxy",
        "@aggregation//:io_netty_netty_resolver",
        "@aggregation//:io_netty_netty_resolver_dns",
        "@aggregation//:io_netty_netty_tcnative_boringssl_static",
        "@aggregation//:io_netty_netty_transport",
    ],
)

java_library(
    name = "io_swagger_swagger_annotations",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_swagger_swagger_annotations",
    ],
)

java_library(
    name = "org_javassist_javassist",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_javassist_javassist",
    ],
)

java_library(
    name = "javax_validation_validation_api",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:javax_validation_validation_api",
    ],
)

java_library(
    name = "joda_time_joda_time",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:joda_time_joda_time",
    ],
)

java_library(
    name = "junit_junit",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:junit_junit",
    ],
)

java_library(
    name = "mysql_mysql_connector_java",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:mysql_mysql_connector_java",
    ],
)

java_library(
    name = "net_bytebuddy_byte_buddy",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:net_bytebuddy_byte_buddy",
    ],
)

java_library(
    name = "net_sourceforge_cssparser_cssparser",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:net_sourceforge_cssparser_cssparser",
        "@aggregation//:org_w3c_css_sac",
    ],
)

java_library(
    name = "net_java_dev_jna_jna",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:net_java_dev_jna_jna",
    ],
)

java_library(
    name = "xerces_xercesImpl",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:xerces_xercesImpl",
    ],
    runtime_deps = [
        "@aggregation//:xml_apis_xml_apis",
    ],
)

java_library(
    name = "net_spy_spymemcached",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:net_spy_spymemcached",
    ],
)

java_library(
    name = "org_apache_commons_commons_csv",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_apache_commons_commons_csv",
    ],
)

java_library(
    name = "org_apache_commons_commons_cli",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:commons_cli_commons_cli",
    ],
)

java_library(
    name = "org_apache_commons_commons_lang",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:commons_lang_commons_lang",
    ],
)

java_library(
    name = "org_apache_commons_commons_lang3",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_apache_commons_commons_lang3",
    ],
)

java_library(
    name = "org_apache_commons_commons_math3",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_apache_commons_commons_math3",
    ],
)

java_library(
    name = "org_apache_zookeeper_zookeeper",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:org_apache_zookeeper_zookeeper",
    ],
    runtime_deps = [],
)

java_library(
    name = "org_apache_curator_curator_client",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:org_apache_curator_curator_client",
    ],
    runtime_deps = [
        ":io_netty_netty_all",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:jline_jline",
        "@aggregation//:log4j_log4j",
        "@aggregation//:org_slf4j_slf4j_api",
        "@aggregation_temp//:org_apache_zookeeper_zookeeper",
    ],
)

java_library(
    name = "org_apache_curator_curator_framework",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:org_apache_curator_curator_framework",
    ],
    runtime_deps = [
        ":io_netty_netty_all",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:jline_jline",
        "@aggregation//:log4j_log4j",
        "@aggregation//:org_slf4j_slf4j_api",
        "@aggregation_temp//:org_apache_curator_curator_client",
        "@aggregation_temp//:org_apache_zookeeper_zookeeper",
    ],
)

java_library(
    name = "org_apache_curator_curator_recipes",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:org_apache_curator_curator_recipes",
    ],
    runtime_deps = [
        ":io_netty_netty_all",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:jline_jline",
        "@aggregation//:log4j_log4j",
        "@aggregation//:org_slf4j_slf4j_api",
        "@aggregation_temp//:org_apache_curator_curator_client",
        "@aggregation_temp//:org_apache_curator_curator_framework",
        "@aggregation_temp//:org_apache_zookeeper_zookeeper",
    ],
)

java_library(
    name = "org_apache_curator_curator_x_discovery",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:org_apache_curator_curator_x_discovery",
    ],
    runtime_deps = [
        ":io_netty_netty_all",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:jline_jline",
        "@aggregation//:log4j_log4j",
        "@aggregation//:org_codehaus_jackson_jackson_mapper_asl",
        "@aggregation//:org_slf4j_slf4j_api",
        "@aggregation_temp//:org_apache_curator_curator_client",
        "@aggregation_temp//:org_apache_curator_curator_framework",
        "@aggregation_temp//:org_apache_curator_curator_recipes",
        "@aggregation_temp//:org_apache_zookeeper_zookeeper",
    ],
)

java_library(
    name = "org_apache_httpcomponents_httpclient",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_apache_httpcomponents_httpclient",
    ],
    runtime_deps = [
        "@aggregation//:commons_codec_commons_codec",
        "@aggregation//:org_apache_httpcomponents_httpcore",
    ],
)

java_import(
    name = "com_amazonaws_aws_java_sdk_sqs",
    jars = [
        # This should be a java_librar w/ exports instead, see:
        # https://github.com/bazelbuild/intellij/issues/66
        "//third_party/aws_java_sdk_core:libaws_java_sdk_core.jar",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:com_amazonaws_aws_java_sdk_code_generator",
        "@aggregation_temp//:com_amazonaws_aws_java_sdk_sqs",
        "@aggregation_temp//:com_amazonaws_jmespath_java",
        "@tink_httpclient_4_5_5",
        "@tink_httpcore_4_4_9",
    ],
    runtime_deps = [
        "//third_party/aws_java_sdk_core",
    ],
)

java_library(
    name = "org_apache_httpcomponents_httpcore",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_apache_httpcomponents_httpcore",
    ],
)

java_library(
    name = "org_assertj_assertj_core",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_assertj_assertj_core",
    ],
)

java_library(
    name = "org_bouncycastle_bcpkix_jdk15on",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_bouncycastle_bcpkix_jdk15on",
        "@aggregation//:org_bouncycastle_bcprov_jdk15on",
    ],
)

java_library(
    name = "org_bouncycastle_bcprov_jdk15on",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_bouncycastle_bcprov_jdk15on",
    ],
)

java_library(
    name = "org_codehaus_plexus_plexus_utils",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_codehaus_plexus_plexus_utils",
    ],
)

java_library(
    name = "org_eclipse_jetty_orbit_javax_servlet",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_eclipse_jetty_orbit_javax_servlet",
    ],
)

java_library(
    name = "org_hibernate_hibernate_annotations",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:antlr_antlr",
        "@aggregation//:commons_collections_commons_collections",
        "@aggregation//:dom4j_dom4j",
        "@aggregation//:javax_transaction_jta",
        "@aggregation//:org_hibernate_hibernate_annotations",
        "@aggregation//:org_hibernate_hibernate_commons_annotations",
        "@aggregation//:org_hibernate_hibernate_core",
        "@aggregation//:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
        "@aggregation//:org_slf4j_slf4j_api",
        "@aggregation//:xml_apis_xml_apis",
    ],
)

java_library(
    name = "org_hibernate_hibernate_validator",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_classmate",
        "@aggregation//:javax_validation_validation_api",
        "@aggregation//:org_hibernate_hibernate_validator",
        "@aggregation//:org_jboss_logging_jboss_logging",
    ],
)

java_library(
    name = "org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
    ],
)

java_library(
    name = "org_iban4j_iban4j",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_iban4j_iban4j",
    ],
)

java_library(
    name = "org_json_json",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_json_json",
    ],
)

java_library(
    name = "org_jsoup_jsoup",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_jsoup_jsoup",
    ],
)

java_library(
    name = "org_modelmapper_modelmapper",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_modelmapper_modelmapper",
    ],
)

java_library(
    name = "org_mozilla_rhino",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_mozilla_rhino",
    ],
)

java_library(
    name = "org_objenesis_objenesis",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_objenesis_objenesis",
    ],
)

java_library(
    name = "org_pojava_pojava",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_pojava_pojava",
    ],
)

java_library(
    name = "org_slf4j_slf4j_api",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "org_springframework_data_spring_data_commons",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_slf4j_slf4j_api",
        "@aggregation//:org_springframework_data_spring_data_commons",
        "@aggregation//:org_springframework_spring_beans",
        "@aggregation//:org_springframework_spring_core",
    ],
)

java_library(
    name = "org_springframework_data_spring_data_jpa",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_springframework_data_spring_data_commons",
        "@aggregation//:org_springframework_data_spring_data_jpa",
        "@aggregation//:org_springframework_spring_aop",
        "@aggregation//:org_springframework_spring_beans",
        "@aggregation//:org_springframework_spring_context",
        "@aggregation//:org_springframework_spring_core",
        "@aggregation//:org_springframework_spring_jdbc",
        "@aggregation//:org_springframework_spring_orm",
        "@aggregation//:org_springframework_spring_tx",
    ],
    runtime_deps = [
        "@aggregation//:org_aspectj_aspectjrt",
        "@aggregation//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "org_springframework_security",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_springframework_security_spring_security_core",
    ],
)

java_library(
    name = "org_springframework_spring_core",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_springframework_spring_core",
    ],
    runtime_deps = [
        "@aggregation//:commons_logging_commons_logging",
    ],
)

java_library(
    name = "org_springframework_spring_expression",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_springframework_spring_expression",
    ],
    runtime_deps = [
        "@aggregation//:org_springframework_spring_core",
    ],
)

java_library(
    name = "org_xerial_snappy_snappy_java",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_xerial_snappy_snappy_java",
    ],
)

java_library(
    name = "org_yaml_snakeyaml",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_yaml_snakeyaml",
    ],
)

java_library(
    name = "pl_pragmatists_junitparams",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:junit_junit",
        "@aggregation//:org_hamcrest_hamcrest_core",
        "@aggregation//:pl_pragmatists_JUnitParams",
    ],
)

java_library(
    name = "org_hamcrest_hamcrest_core",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_hamcrest_hamcrest_core",
    ],
)

java_library(
    name = "org_hamcrest_hamcrest_library",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_hamcrest_hamcrest_library",
    ],
)

java_library(
    name = "org_reflections_reflections",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_reflections_reflections",
    ],
    runtime_deps = [
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:dom4j_dom4j",
        "@aggregation//:javax_servlet_javax_servlet_api",
        "@aggregation//:junit_junit",
        "@aggregation//:org_javassist_javassist",
        "@aggregation//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "org_apache_pdfbox_pdfbox",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_apache_pdfbox_pdfbox",
    ],
)

java_library(
    name = "org_apache_pdfbox_fontbox",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_apache_pdfbox_fontbox",
    ],
)

java_library(
    name = "com_nimbusds_srp6a",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_nimbusds_srp6a",
    ],
)

java_library(
    name = "com_nimbusds_nimbus_jose_jwt",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:com_nimbusds_nimbus_jose_jwt",
    ],
    runtime_deps = [
        "@aggregation//:com_github_stephenc_jcip_jcip_annotations",
        "@aggregation//:junit_junit",
        "@aggregation//:net_jadler_jadler_all",
        "@aggregation//:net_minidev_json_smart",
        "@aggregation//:org_bitbucket_b_c_jose4j",
        "@aggregation//:org_bouncycastle_bcpkix_jdk15on",
        "@aggregation//:org_bouncycastle_bcprov_jdk15on",
        "@aggregation_temp//:com_google_crypto_tink_tink",
    ],
)

java_library(
    name = "com_auth0_java_jwt",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:com_auth0_java_jwt",
    ],
)

java_library(
    name = "net_sourceforge_tess4j",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_github_jai_imageio_jai_imageio_core",
        "@aggregation//:net_java_dev_jna_jna",
        "@aggregation_temp//:net_sourceforge_lept4j_lept4j",
        "@aggregation_temp//:net_sourceforge_tess4j_tess4j",
    ],
)

java_library(
    name = "log",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_yaml",
        "@aggregation//:org_apache_logging_log4j_log4j_api",
        "@aggregation//:org_apache_logging_log4j_log4j_core",
    ],
)

java_library(
    name = "log-runtime",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:org_yaml_snakeyaml",
    ],
)

java_library(
    name = "netty",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_api_grpc_proto_google_common_protos",
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_google_instrumentation_instrumentation_api",
        "@aggregation//:com_google_protobuf_protobuf_java",
        "@aggregation//:com_google_protobuf_protobuf_java_util",
        "@aggregation//:io_grpc_grpc_context",
        "@aggregation//:io_netty_netty_buffer",
        "@aggregation//:io_netty_netty_codec",
        "@aggregation//:io_netty_netty_codec_dns",
        "@aggregation//:io_netty_netty_codec_http",
        "@aggregation//:io_netty_netty_codec_http2",
        "@aggregation//:io_netty_netty_codec_socks",
        "@aggregation//:io_netty_netty_common",
        "@aggregation//:io_netty_netty_handler",
        "@aggregation//:io_netty_netty_handler_proxy",
        "@aggregation//:io_netty_netty_resolver",
        "@aggregation//:io_netty_netty_resolver_dns",
        "@aggregation//:io_netty_netty_transport",
        "@aggregation//:io_opencensus_opencensus_api",
        "@aggregation_temp//:io_grpc_grpc_api",
        "@aggregation_temp//:io_grpc_grpc_auth",
        "@aggregation_temp//:io_grpc_grpc_core",
        "@aggregation_temp//:io_grpc_grpc_netty",
        "@aggregation_temp//:io_grpc_grpc_protobuf",
        "@aggregation_temp//:io_grpc_grpc_protobuf_lite",
        "@aggregation_temp//:io_grpc_grpc_protobuf_nano",
        "@aggregation_temp//:io_grpc_grpc_stub",
        "@aggregation_temp//:io_grpc_grpc_testing",
        "@aggregation_temp//:io_opencensus_opencensus_contrib_grpc_metrics",
    ],
)

java_library(
    name = "netty-runtime",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_netty_netty_tcnative_boringssl_static",
    ],
)

java_library(
    name = "prometheus",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_prometheus_simpleclient",
        "@aggregation//:io_prometheus_simpleclient_common",
        "@aggregation//:io_prometheus_simpleclient_hotspot",
        "@aggregation//:io_prometheus_simpleclient_httpserver",
        "@aggregation//:io_prometheus_simpleclient_pushgateway",
    ],
)

java_library(
    name = "zxing-qrcode",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_zxing_core",
        "@aggregation//:com_google_zxing_javase",
    ],
)

java_library(
    name = "bouncycastle",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_bouncycastle_bcpkix_jdk15on",
        "@aggregation//:org_bouncycastle_bcprov_jdk15on",
    ],
)

java_library(
    name = "io_vavr",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_vavr_vavr",
        "@aggregation//:io_vavr_vavr_jackson",
        "@aggregation//:io_vavr_vavr_match",
        "@aggregation//:io_vavr_vavr_test",
    ],
)

java_library(
    name = "com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
    ],
)

java_library(
    name = "io_grpc_grpc_testing_proto",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:io_grpc_grpc_testing_proto",
    ],
    runtime_deps = [
        "@aggregation_temp//:io_grpc_grpc_protobuf",
        "@aggregation_temp//:io_grpc_grpc_stub",
    ],
)

java_library(
    name = "io_grpc_grpc_netty",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:io_grpc_grpc_netty",
    ],
    runtime_deps = [
        "@aggregation//:io_netty_netty_codec_http2",
        "@aggregation//:io_netty_netty_handler_proxy",
        "@aggregation_temp//:io_grpc_grpc_core",
    ],
)

java_library(
    name = "io_grpc_grpc_context",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_grpc_grpc_context",
    ],
)

java_library(
    name = "io_grpc_grpc_stub",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:io_grpc_grpc_stub",
    ],
    runtime_deps = [
        "@aggregation_temp//:io_grpc_grpc_api",
    ],
)

java_library(
    name = "io_grpc_grpc_testing",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation_temp//:io_grpc_grpc_testing",
    ],
    runtime_deps = [
        "@aggregation//:junit_junit",
        "@aggregation_temp//:io_grpc_grpc_core",
        "@aggregation_temp//:io_grpc_grpc_stub",
    ],
)

java_library(
    name = "io_netty_netty_codec_http2",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_netty_netty_codec_http2",
    ],
    runtime_deps = [
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_jcraft_jzlib",
        "@aggregation//:io_netty_netty_buffer",
        "@aggregation//:io_netty_netty_codec",
        "@aggregation//:io_netty_netty_codec_http",
        "@aggregation//:io_netty_netty_common",
        "@aggregation//:io_netty_netty_handler",
        "@aggregation//:io_netty_netty_transport",
        "@aggregation//:org_mockito_mockito_core",
    ],
)

java_library(
    name = "io_netty_netty_common",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_netty_netty_common",
    ],
    runtime_deps = [
        "@aggregation//:commons_logging_commons_logging",
        "@aggregation//:io_netty_netty_dev_tools",
        "@aggregation//:log4j_log4j",
        "@aggregation//:org_apache_logging_log4j_log4j_api",
        "@aggregation//:org_apache_logging_log4j_log4j_core",
        "@aggregation//:org_mockito_mockito_core",
        "@aggregation//:org_slf4j_slf4j_api",
        "@aggregation_temp//:com_oracle_substratevm_svm",
    ],
)

java_library(
    name = "io_netty_netty_handler",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_netty_netty_handler",
    ],
    runtime_deps = [
        "@aggregation//:io_netty_netty_buffer",
        "@aggregation//:io_netty_netty_codec",
        "@aggregation//:io_netty_netty_tcnative_boringssl_static",
        "@aggregation//:io_netty_netty_transport",
        "@aggregation//:org_bouncycastle_bcpkix_jdk15on",
        "@aggregation//:org_mockito_mockito_core",
    ],
)

java_library(
    name = "io_netty_netty_tcnative_boringssl_static",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_netty_netty_tcnative_boringssl_static",
    ],
)

java_library(
    name = "org_mockito_mockito_core",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_mockito_mockito_core",
    ],
    runtime_deps = [
        "@aggregation//:net_bytebuddy_byte_buddy",
        "@aggregation//:net_bytebuddy_byte_buddy_agent",
        "@aggregation//:org_objenesis_objenesis",
    ],
)

java_library(
    name = "io_netty_netty_resolver",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:io_netty_netty_resolver",
    ],
    runtime_deps = [
        "@aggregation//:io_netty_netty_common",
        "@aggregation//:org_mockito_mockito_core",
    ],
)

java_library(
    name = "net_minidev_json_smart",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:net_minidev_json_smart",
    ],
    runtime_deps = [
        "@aggregation//:junit_junit",
        "@aggregation//:net_minidev_asm",
    ],
)

java_library(
    name = "jetty_server9",
    licenses = ["notice"],
    visibility = ["//visibility:public"],
    exports = [
        "@jetty_server9//:javax_servlet_javax_servlet_api",
        "@jetty_server9//:org_eclipse_jetty_jetty_http",
        "@jetty_server9//:org_eclipse_jetty_jetty_server",
        "@jetty_server9//:org_eclipse_jetty_jetty_util",
    ],
)

java_plugin(
    name = "lombok_plugin",
    generates_api = 1,
    processor_class = "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
    visibility = ["//visibility:public"],
    deps = [
        "@aggregation//:org_projectlombok_lombok",
    ],
)

java_library(
    name = "lombok_libs",
    licenses = ["notice"],
    visibility = ["//visibility:public"],
    exports = [
        "//third_party:lombok_plugin",
        "@aggregation//:org_projectlombok_lombok",
    ],
)
