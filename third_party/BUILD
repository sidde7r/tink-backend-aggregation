load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "asm_asm",
    visibility = ["//visibility:public"],
    exports = [
        "@asm_asm//jar",
    ],
)

java_library(
    name = "c3p0_c3p0",
    visibility = ["//visibility:public"],
    exports = [
        "@c3p0_c3p0//jar",
    ],
)

java_library(
    name = "ch_qos_logback_logback_classic",
    visibility = ["//visibility:public"],
    exports = [
        "@ch_qos_logback_logback_classic//jar",
    ],
    runtime_deps = [
        "@aggregation//:org_slf4j_slf4j_api",
        "@ch_qos_logback_logback_core//jar",
    ],
)

java_library(
    name = "ch_qos_logback_contrib_logback_jackson",
    visibility = ["//visibility:public"],
    exports = [
        "@ch_qos_logback_contrib_logback_jackson//jar",
    ],
)

java_library(
    name = "ch_qos_logback_contrib_logback_json_classic",
    visibility = ["//visibility:public"],
    exports = [
        "@ch_qos_logback_contrib_logback_json_classic//jar",
    ],
    runtime_deps = [
        "@ch_qos_logback_contrib_logback_jackson//jar",
        "@ch_qos_logback_contrib_logback_json_core//jar",
    ],
)

java_library(
    name = "ch_qos_logback_contrib_logback_json_core",
    visibility = ["//visibility:public"],
    exports = [
        "@ch_qos_logback_contrib_logback_json_core//jar",
    ],
)

java_library(
    name = "com_amazonaws_aws_java_sdk_s3",
    visibility = ["//visibility:public"],
    exports = [
        "@com_amazonaws_aws_java_sdk_s3//jar",
    ],
    runtime_deps = [
        "//third_party/aws_java_sdk_core",
        "@com_amazonaws_aws_java_sdk_code_generator//jar",
        "@com_amazonaws_aws_java_sdk_kms//jar",
        "@com_amazonaws_jmespath_java//jar",
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
        "@com_github_rholder_guava_retrying//jar",
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
        "@com_github_tomakehurst_wiremock//jar",
    ],
    runtime_deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:commons_codec_commons_codec",
        "@aggregation//:commons_logging_commons_logging",
        "@aggregation//:org_apache_commons_commons_lang3",
        "@aggregation//:org_apache_httpcomponents_httpcore",
        "@aggregation//:org_slf4j_slf4j_api",
        "@asm_asm//jar",
        "@com_flipkart_zjsonpatch_zjsonpatch//jar",
        "@com_jayway_jsonpath_json_path//jar",
        "@javax_servlet_javax_servlet_api//jar",
        "@junit_junit//jar",
        "@net_minidev_asm//jar",
        "@net_minidev_json_smart//jar",
        "@net_sf_jopt_simple_jopt_simple//jar",
        "@org_apache_commons_commons_collections4//jar",
        "@org_apache_httpcomponents_httpclient//jar",
        "@org_eclipse_jetty_jetty_continuation//jar",
        "@org_eclipse_jetty_jetty_http//jar",
        "@org_eclipse_jetty_jetty_io//jar",
        "@org_eclipse_jetty_jetty_security//jar",
        "@org_eclipse_jetty_jetty_server//jar",
        "@org_eclipse_jetty_jetty_servlet//jar",
        "@org_eclipse_jetty_jetty_servlets//jar",
        "@org_eclipse_jetty_jetty_util//jar",
        "@org_eclipse_jetty_jetty_webapp//jar",
        "@org_eclipse_jetty_jetty_xml//jar",
        "@org_xmlunit_xmlunit_core//jar",
        "@org_xmlunit_xmlunit_legacy//jar",
    ],
)

java_library(
    name = "org_apache_commons_commons_collections4",
    visibility = ["//visibility:public"],
    exports = [
        "@org_apache_commons_commons_collections4//jar",
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
        "@com_google_http_client_google_http_client//jar",
    ],
    runtime_deps = [
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@org_apache_httpcomponents_httpclient//jar",
    ],
)

java_library(
    name = "com_google_inject_extensions_guice_multibindings",
    visibility = ["//visibility:public"],
    exports = [
        "@com_google_inject_extensions_guice_multibindings//jar",
    ],
    runtime_deps = [
        "@com_google_inject_guice//jar",
    ],
)

java_library(
    name = "com_google_inject_guice",
    visibility = ["//visibility:public"],
    exports = [
        "@com_google_inject_guice//jar",
    ],
    runtime_deps = [
        "@aggregation//:com_google_guava_guava",
        "@aopalliance_aopalliance//jar",
        "@javax_inject_javax_inject//jar",
    ],
)

java_library(
    name = "javax_inject_javax_inject",
    visibility = ["//visibility:public"],
    exports = [
        "@javax_inject_javax_inject//jar",
    ],
)

java_library(
    name = "com_google_protobuf_protobuf_java",
    visibility = ["//visibility:public"],
    exports = [
        "@com_google_protobuf_protobuf_java//jar",
    ],
)

java_library(
    name = "com_netflix_governator",
    visibility = ["//visibility:public"],
    exports = [
        "@com_netflix_governator//jar",
    ],
    runtime_deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@com_google_inject_extensions_guice_assistedinject//jar",
        "@com_google_inject_extensions_guice_grapher//jar",
        "@com_google_inject_extensions_guice_multibindings//jar",
        "@com_google_inject_guice//jar",
        "@com_netflix_governator_api//jar",
        "@com_netflix_governator_core//jar",
        "@javax_inject_javax_inject//jar",
    ],
)

java_library(
    name = "com_google_instrumentation_instrumentation_api",
    visibility = ["//visibility:public"],
    exports = [
        "@com_google_instrumentation_instrumentation_api//jar",
    ],
    runtime_deps = [
        "@aggregation//:com_google_code_findbugs_jsr305",
    ],
)

java_library(
    name = "com_googlecode_gettext_commons_gettext_commons",
    visibility = ["//visibility:public"],
    exports = [
        "@com_googlecode_gettext_commons_gettext_commons//jar",
    ],
)

java_library(
    name = "com_lambdaworks_scrypt",
    visibility = ["//visibility:public"],
    exports = [
        "@com_lambdaworks_scrypt//jar",
    ],
)

java_library(
    name = "com_sun_jersey_contribs_jersey_apache_client4",
    visibility = ["//visibility:public"],
    exports = [
        "@com_sun_jersey_contribs_jersey_apache_client4//jar",
    ],
    runtime_deps = [
        "@com_sun_jersey_jersey_client//jar",
    ],
)

java_library(
    name = "com_sun_jersey_jersey_client",
    visibility = ["//visibility:public"],
    exports = [
        "@com_sun_jersey_jersey_client//jar",
    ],
    runtime_deps = [
        "@com_sun_jersey_jersey_core//jar",
    ],
)

java_library(
    name = "com_sun_jersey_jersey_core",
    visibility = ["//visibility:public"],
    exports = [
        "@com_sun_jersey_jersey_core//jar",
    ],
)

java_library(
    name = "com_sun_jersey_jersey_server",
    visibility = ["//visibility:public"],
    exports = [
        "@com_sun_jersey_jersey_server//jar",
    ],
    runtime_deps = [
        "@com_sun_jersey_jersey_core//jar",
    ],
)

java_library(
    name = "com_yubico_yubico_validation_client2",
    visibility = ["//visibility:public"],
    exports = [
        "@com_yubico_yubico_validation_client2//jar",
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
        "@commons_httpclient_commons_httpclient//jar",
    ],
)

java_library(
    name = "commons_io_commons_io",
    visibility = ["//visibility:public"],
    exports = [
        "@commons_io_commons_io//jar",
    ],
)

java_library(
    name = "de_jollyday_jollyday",
    visibility = ["//visibility:public"],
    exports = [
        "@de_jollyday_jollyday//jar",
    ],
    runtime_deps = [
        "@aggregation//:joda_time_joda_time",
        "@javax_xml_bind_jaxb_api//jar",
    ],
)

java_library(
    name = "eu_geekplace_javapinning_java_pinning_jar",
    visibility = ["//visibility:public"],
    exports = [
        "@eu_geekplace_javapinning_java_pinning_jar//jar",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_client",
    visibility = ["//visibility:public"],
    exports = [
        "@io_dropwizard_dropwizard_client//jar",
    ],
    runtime_deps = [
        "@aggregation//:commons_codec_commons_codec",
        "@aggregation//:org_apache_httpcomponents_httpcore",
        "@com_codahale_metrics_metrics_core//jar",
        "@com_codahale_metrics_metrics_httpclient//jar",
        "@com_sun_jersey_contribs_jersey_apache_client4//jar",
        "@com_sun_jersey_jersey_client//jar",
        "@io_dropwizard_dropwizard_core//jar",
        "@org_apache_httpcomponents_httpclient//jar",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_configuration",
    visibility = ["//visibility:public"],
    exports = [
        "@io_dropwizard_dropwizard_configuration//jar",
    ],
    runtime_deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_yaml",
        "@commons_lang_commons_lang//jar",
        "@io_dropwizard_dropwizard_jackson//jar",
        "@io_dropwizard_dropwizard_validation//jar",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_core",
    visibility = ["//visibility:public"],
    exports = [
        "@io_dropwizard_dropwizard_core//jar",
    ],
    runtime_deps = [
        ":io_dropwizard_dropwizard_jersey",
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
        "@aggregation//:org_slf4j_slf4j_api",
        "@aggregation//:org_yaml_snakeyaml",
        "@ch_qos_logback_logback_classic//jar",
        "@ch_qos_logback_logback_core//jar",
        "@com_codahale_metrics_metrics_annotation//jar",
        "@com_codahale_metrics_metrics_core//jar",
        "@com_codahale_metrics_metrics_healthchecks//jar",
        "@com_codahale_metrics_metrics_jersey//jar",
        "@com_codahale_metrics_metrics_jetty9//jar",
        "@com_codahale_metrics_metrics_json//jar",
        "@com_codahale_metrics_metrics_jvm//jar",
        "@com_codahale_metrics_metrics_logback//jar",
        "@com_codahale_metrics_metrics_servlets//jar",
        "@com_sun_jersey_jersey_core//jar",
        "@com_sun_jersey_jersey_server//jar",
        "@com_sun_jersey_jersey_servlet//jar",
        "@commons_lang_commons_lang//jar",
        "@io_dropwizard_dropwizard_configuration//jar",
        "@io_dropwizard_dropwizard_jackson//jar",
        "@io_dropwizard_dropwizard_jetty//jar",
        "@io_dropwizard_dropwizard_lifecycle//jar",
        "@io_dropwizard_dropwizard_logging//jar",
        "@io_dropwizard_dropwizard_metrics//jar",
        "@io_dropwizard_dropwizard_servlets//jar",
        "@io_dropwizard_dropwizard_util//jar",
        "@io_dropwizard_dropwizard_validation//jar",
        "@javax_el_javax_el_api//jar",
        "@javax_validation_validation_api//jar",
        "@net_sourceforge_argparse4j_argparse4j//jar",
        "@org_eclipse_jetty_jetty_continuation//jar",
        "@org_eclipse_jetty_jetty_http//jar",
        "@org_eclipse_jetty_jetty_io//jar",
        "@org_eclipse_jetty_jetty_security//jar",
        "@org_eclipse_jetty_jetty_server//jar",
        "@org_eclipse_jetty_jetty_servlet//jar",
        "@org_eclipse_jetty_jetty_servlets//jar",
        "@org_eclipse_jetty_jetty_util//jar",
        "@org_eclipse_jetty_orbit_javax_servlet//jar",
        "@org_eclipse_jetty_toolchain_setuid_jetty_setuid_java//jar",
        "@org_glassfish_web_javax_el//jar",
        "@org_hibernate_hibernate_validator//jar",
        "@org_jboss_logging_jboss_logging//jar",
        "@org_slf4j_jcl_over_slf4j//jar",
        "@org_slf4j_jul_to_slf4j//jar",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_servlets",
    visibility = ["//visibility:public"],
    exports = [
        "@io_dropwizard_dropwizard_servlets//jar",
    ],
    runtime_deps = [
        "@ch_qos_logback_logback_classic//jar",
        "@com_codahale_metrics_metrics_core//jar",
        "@org_eclipse_jetty_jetty_servlet//jar",
        "@org_eclipse_jetty_orbit_javax_servlet//jar",
    ],
)

java_library(
    name = "com_codahale_metrics_metrics_healthchecks",
    visibility = ["//visibility:public"],
    exports = [
        "@com_codahale_metrics_metrics_healthchecks//jar",
    ],
)

java_library(
    name = "net_sourceforge_argparse4j_argparse4j",
    visibility = ["//visibility:public"],
    exports = [
        "@net_sourceforge_argparse4j_argparse4j//jar",
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
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_base",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
        "@aggregation//:com_fasterxml_jackson_module_jackson_module_jaxb_annotations",
        "@com_codahale_metrics_metrics_annotation//jar",
        "@com_codahale_metrics_metrics_core//jar",
        "@com_codahale_metrics_metrics_jersey//jar",
        "@com_sun_jersey_jersey_core//jar",
        "@com_sun_jersey_jersey_server//jar",
        "@com_sun_jersey_jersey_servlet//jar",
        "@io_dropwizard_dropwizard_jackson//jar",
        "@io_dropwizard_dropwizard_logging//jar",
        "@io_dropwizard_dropwizard_validation//jar",
        "@org_eclipse_jetty_orbit_javax_servlet//jar",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_lifecycle",
    visibility = ["//visibility:public"],
    exports = [
        "@io_dropwizard_dropwizard_lifecycle//jar",
    ],
    runtime_deps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_slf4j_slf4j_api",
        "@io_dropwizard_dropwizard_util//jar",
        "@org_eclipse_jetty_jetty_http//jar",
        "@org_eclipse_jetty_jetty_io//jar",
        "@org_eclipse_jetty_jetty_server//jar",
        "@org_eclipse_jetty_jetty_util//jar",
        "@org_eclipse_jetty_orbit_javax_servlet//jar",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_logging",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_slf4j_slf4j_api",
        "@ch_qos_logback_logback_classic//jar",
        "@ch_qos_logback_logback_core//jar",
        "@com_codahale_metrics_metrics_core//jar",
        "@com_codahale_metrics_metrics_logback//jar",
        "@io_dropwizard_dropwizard_jackson//jar",
        "@io_dropwizard_dropwizard_logging//jar",
        "@io_dropwizard_dropwizard_validation//jar",
        "@org_eclipse_jetty_jetty_util//jar",
        "@org_slf4j_jcl_over_slf4j//jar",
        "@org_slf4j_jul_to_slf4j//jar",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_metrics",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_slf4j_slf4j_api",
        "@com_codahale_metrics_metrics_core//jar",
        "@io_dropwizard_dropwizard_jackson//jar",
        "@io_dropwizard_dropwizard_lifecycle//jar",
        "@io_dropwizard_dropwizard_metrics//jar",
        "@io_dropwizard_dropwizard_util//jar",
        "@io_dropwizard_dropwizard_validation//jar",
        "@org_eclipse_jetty_jetty_http//jar",
        "@org_eclipse_jetty_jetty_io//jar",
        "@org_eclipse_jetty_jetty_server//jar",
        "@org_eclipse_jetty_jetty_util//jar",
        "@org_eclipse_jetty_orbit_javax_servlet//jar",
    ],
)

java_library(
    name = "io_grpc_grpc_core",
    visibility = ["//visibility:public"],
    exports = [
        "@io_grpc_grpc_api//jar",
        "@io_grpc_grpc_core//jar",
    ],
    runtime_deps = [
        "@io_perfmark_perfmark_api//jar",
    ],
)

java_library(
    # is it necessary ?
    name = "io_grpc_grpc_protobuf_lite",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_guava_guava",
        "@io_grpc_grpc_api//jar",
        "@io_grpc_grpc_core//jar",
        "@io_grpc_grpc_protobuf_lite//jar",
        "@io_grpc_grpc_protobuf_nano//jar",
        "@io_opencensus_opencensus_api//jar",
        "@io_opencensus_opencensus_contrib_grpc_metrics//jar",
    ],
)

java_library(
    name = "io_netty_netty_codec",
    visibility = ["//visibility:public"],
    exports = [
        "@io_netty_netty_buffer//jar",
        "@io_netty_netty_codec//jar",
        "@io_netty_netty_common//jar",
        "@io_netty_netty_resolver//jar",
        "@io_netty_netty_transport//jar",
    ],
)

java_library(
    name = "io_netty_netty_codec_http",
    visibility = ["//visibility:public"],
    exports = [
        "@io_netty_netty_buffer//jar",
        "@io_netty_netty_codec//jar",
        "@io_netty_netty_codec_http//jar",
        "@io_netty_netty_common//jar",
        "@io_netty_netty_resolver//jar",
        "@io_netty_netty_transport//jar",
    ],
)

java_library(
    name = "io_netty_netty_all",
    visibility = ["//visibility:public"],
    exports = [
        "@io_netty_netty_buffer//jar",
        "@io_netty_netty_codec//jar",
        "@io_netty_netty_codec_dns//jar",
        "@io_netty_netty_codec_http//jar",
        "@io_netty_netty_codec_http2//jar",
        "@io_netty_netty_codec_socks//jar",
        "@io_netty_netty_common//jar",
        "@io_netty_netty_handler//jar",
        "@io_netty_netty_handler_proxy//jar",
        "@io_netty_netty_resolver//jar",
        "@io_netty_netty_resolver_dns//jar",
        "@io_netty_netty_tcnative_boringssl_static//jar",
        "@io_netty_netty_transport//jar",
    ],
)

java_library(
    name = "io_prometheus_simpleclient_servlet",
    visibility = ["//visibility:public"],
    exports = [
        "@io_prometheus_simpleclient//jar",
        "@io_prometheus_simpleclient_common//jar",
        "@io_prometheus_simpleclient_servlet//jar",
    ],
)

java_library(
    name = "io_protostuff_protostuff_api",
    visibility = ["//visibility:public"],
    exports = [
        "@io_protostuff_protostuff_api//jar",
    ],
)

java_library(
    name = "io_swagger_swagger_annotations",
    visibility = ["//visibility:public"],
    exports = [
        "@io_swagger_swagger_annotations//jar",
    ],
)

java_library(
    name = "org_javassist_javassist",
    visibility = ["//visibility:public"],
    exports = [
        "@org_javassist_javassist//jar",
    ],
)

java_library(
    name = "javax_validation_validation_api",
    visibility = ["//visibility:public"],
    exports = [
        "@javax_validation_validation_api//jar",
    ],
)

java_library(
    # TODO: check is this used?
    name = "jfree_jcommon",
    visibility = ["//visibility:public"],
    exports = [
        "@jfree_jcommon//jar",
        "@junit_junit//jar",
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
        "@junit_junit//jar",
    ],
)

java_library(
    name = "mysql_mysql_connector_java",
    visibility = ["//visibility:public"],
    exports = [
        "@mysql_mysql_connector_java//jar",
    ],
)

java_library(
    name = "net_bytebuddy_byte_buddy",
    visibility = ["//visibility:public"],
    exports = [
        "@net_bytebuddy_byte_buddy//jar",
    ],
)

java_library(
    name = "net_sourceforge_cssparser_cssparser",
    visibility = ["//visibility:public"],
    exports = [
        "@net_sourceforge_cssparser_cssparser//jar",
        "@org_w3c_css_sac//jar",
    ],
)

java_library(
    name = "net_java_dev_jna_jna",
    visibility = ["//visibility:public"],
    exports = [
        "@net_java_dev_jna_jna//jar",
    ],
)

java_library(
    name = "xerces_xercesImpl",
    visibility = ["//visibility:public"],
    exports = [
        "@xerces_xercesImpl//jar",
    ],
    runtime_deps = [
        "@xml_apis_xml_apis//jar",
    ],
)

java_library(
    # TODO: check is this used?
    name = "net_sourceforge_htmlunit_htmlunit",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:commons_codec_commons_codec",
        "@aggregation//:commons_logging_commons_logging",
        "@aggregation//:org_apache_commons_commons_lang3",
        "@aggregation//:org_apache_httpcomponents_httpcore",
        "@commons_collections_commons_collections//jar",
        "@commons_io_commons_io//jar",
        "@net_sourceforge_cssparser_cssparser//jar",
        "@net_sourceforge_htmlunit_htmlunit//jar",
        "@net_sourceforge_htmlunit_htmlunit_core_js//jar",
        "@net_sourceforge_nekohtml_nekohtml//jar",
        "@org_apache_httpcomponents_httpclient//jar",
        "@org_apache_httpcomponents_httpmime//jar",
        "@org_eclipse_jetty_jetty_http//jar",
        "@org_eclipse_jetty_jetty_io//jar",
        "@org_eclipse_jetty_jetty_util//jar",
        "@org_eclipse_jetty_jetty_websocket//jar",
        "@org_w3c_css_sac//jar",
        "@xalan_serializer//jar",
        "@xalan_xalan//jar",
        "@xerces_xercesImpl//jar",
        "@xml_apis_xml_apis//jar",
    ],
)

java_library(
    name = "net_spy_spymemcached",
    visibility = ["//visibility:public"],
    exports = [
        "@net_spy_spymemcached//jar",
    ],
)

java_library(
    # TODO: check is this used?
    name = "oauth_signpost_signpost_commonshttp4",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_apache_httpcomponents_httpcore",
        "@oauth_signpost_signpost_commonshttp4//jar",
        "@oauth_signpost_signpost_core//jar",
        "@org_apache_httpcomponents_httpclient//jar",
    ],
)

java_library(
    name = "oauth_signpost_signpost_core",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:commons_codec_commons_codec",
        "@oauth_signpost_signpost_core//jar",
    ],
)

java_library(
    name = "org_apache_commons_commons_csv",
    visibility = ["//visibility:public"],
    exports = [
        "@org_apache_commons_commons_csv//jar",
    ],
)

java_library(
    name = "org_apache_commons_commons_cli",
    visibility = ["//visibility:public"],
    exports = [
        "@commons_cli_commons_cli//jar",
    ],
)

java_library(
    name = "org_apache_commons_commons_lang",
    visibility = ["//visibility:public"],
    exports = [
        "@commons_lang_commons_lang//jar",
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
        "@org_apache_commons_commons_math3//jar",
    ],
)

java_library(
    name = "org_apache_zookeeper_zookeeper",
    visibility = ["//visibility:public"],
    exports = [
        "@org_apache_zookeeper_zookeeper//jar",
    ],
    runtime_deps = [],
)

java_library(
    name = "org_apache_curator_curator_client",
    visibility = ["//visibility:public"],
    exports = [
        "@org_apache_curator_curator_client//jar",
    ],
    runtime_deps = [
        ":io_netty_netty_all",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_slf4j_slf4j_api",
        "@jline_jline//jar",
        "@log4j_log4j//jar",
        "@org_apache_zookeeper_zookeeper//jar",
    ],
)

java_library(
    name = "org_apache_curator_curator_framework",
    visibility = ["//visibility:public"],
    exports = [
        "@org_apache_curator_curator_framework//jar",
    ],
    runtime_deps = [
        ":io_netty_netty_all",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_slf4j_slf4j_api",
        "@jline_jline//jar",
        "@log4j_log4j//jar",
        "@org_apache_curator_curator_client//jar",
        "@org_apache_zookeeper_zookeeper//jar",
    ],
)

java_library(
    name = "org_apache_curator_curator_recipes",
    visibility = ["//visibility:public"],
    exports = [
        "@org_apache_curator_curator_recipes//jar",
    ],
    runtime_deps = [
        ":io_netty_netty_all",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_slf4j_slf4j_api",
        "@jline_jline//jar",
        "@log4j_log4j//jar",
        "@org_apache_curator_curator_client//jar",
        "@org_apache_curator_curator_framework//jar",
        "@org_apache_zookeeper_zookeeper//jar",
    ],
)

java_library(
    name = "org_apache_curator_curator_x_discovery",
    visibility = ["//visibility:public"],
    exports = [
        "@org_apache_curator_curator_x_discovery//jar",
    ],
    runtime_deps = [
        ":io_netty_netty_all",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_slf4j_slf4j_api",
        "@jline_jline//jar",
        "@log4j_log4j//jar",
        "@org_apache_curator_curator_client//jar",
        "@org_apache_curator_curator_framework//jar",
        "@org_apache_curator_curator_recipes//jar",
        "@org_apache_zookeeper_zookeeper//jar",
        "@org_codehaus_jackson_jackson_mapper_asl//jar",
    ],
)

java_library(
    name = "org_apache_httpcomponents_httpclient",
    visibility = ["//visibility:public"],
    exports = [
        "@org_apache_httpcomponents_httpclient//jar",
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
        "@com_amazonaws_aws_java_sdk_code_generator//jar",
        "@com_amazonaws_aws_java_sdk_sqs//jar",
        "@com_amazonaws_jmespath_java//jar",
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
    # is this used?
    name = "org_apache_kafka_kafka_streams",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:org_slf4j_slf4j_api",
        "@net_jpountz_lz4_lz4//jar",
        "@org_apache_kafka_connect_api//jar",
        "@org_apache_kafka_connect_json//jar",
        "@org_apache_kafka_kafka_clients//jar",
        "@org_apache_kafka_kafka_streams//jar",
        "@org_xerial_snappy_snappy_java//jar",
    ],
)

java_library(
    # TODO: check is this used?
    name = "org_apache_lucene_lucene_core",
    visibility = ["//visibility:public"],
    exports = [
        "@org_apache_lucene_lucene_core//jar",
    ],
)

java_library(
    # TODO: check is this used?
    name = "org_apache_mahout_mahout_core",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_apache_commons_commons_lang3",
        "@aggregation//:org_slf4j_slf4j_api",
        "@com_thoughtworks_xstream_xstream//jar",
        "@org_apache_commons_commons_math3//jar",
        "@org_apache_lucene_lucene_analyzers_common//jar",
        "@org_apache_lucene_lucene_core//jar",
        "@org_apache_mahout_commons_commons_cli//jar",
        "@org_apache_mahout_mahout_core//jar",
        "@org_apache_mahout_mahout_math//jar",
        "@org_codehaus_jackson_jackson_core_asl//jar",
        "@org_codehaus_jackson_jackson_mapper_asl//jar",
        "@xmlpull_xmlpull//jar",
        "@xpp3_xpp3_min//jar",
    ],
)

java_library(
    name = "org_apache_velocity_velocity",
    visibility = ["//visibility:public"],
    exports = [
        "@commons_collections_commons_collections//jar",
        "@commons_lang_commons_lang//jar",
        "@org_apache_velocity_velocity//jar",
    ],
)

java_library(
    name = "org_assertj_assertj_core",
    visibility = ["//visibility:public"],
    exports = [
        "@org_assertj_assertj_core//jar",
    ],
)

java_library(
    name = "org_bouncycastle_bcpkix_jdk15on",
    visibility = ["//visibility:public"],
    exports = [
        "@org_bouncycastle_bcpkix_jdk15on//jar",
        "@org_bouncycastle_bcprov_jdk15on//jar",
    ],
)

java_library(
    name = "org_bouncycastle_bcprov_jdk15on",
    visibility = ["//visibility:public"],
    exports = [
        "@org_bouncycastle_bcprov_jdk15on//jar",
    ],
)

java_library(
    name = "org_codehaus_jackson_jackson_core_asl",
    visibility = ["//visibility:public"],
    exports = [
        "@org_codehaus_jackson_jackson_core_asl//jar",
    ],
)

java_library(
    name = "org_codehaus_jackson_jackson",
    visibility = ["//visibility:public"],
    exports = [
        "@org_codehaus_jackson_jackson_core_asl//jar",
        "@org_codehaus_jackson_jackson_jaxrs//jar",
        "@org_codehaus_jackson_jackson_mapper_asl//jar",
        "@org_codehaus_jackson_jackson_xc//jar",
    ],
)

java_library(
    name = "org_codehaus_plexus_plexus_utils",
    visibility = ["//visibility:public"],
    exports = [
        "@org_codehaus_plexus_plexus_utils//jar",
    ],
)

java_library(
    name = "org_eclipse_jetty_jetty_servlets",
    visibility = ["//visibility:public"],
    exports = [
        "@org_eclipse_jetty_jetty_continuation//jar",
        "@org_eclipse_jetty_jetty_http//jar",
        "@org_eclipse_jetty_jetty_io//jar",
        "@org_eclipse_jetty_jetty_servlets//jar",
        "@org_eclipse_jetty_jetty_util//jar",
    ],
)

java_library(
    name = "org_eclipse_jetty_orbit_javax_servlet",
    visibility = ["//visibility:public"],
    exports = [
        "@org_eclipse_jetty_orbit_javax_servlet//jar",
    ],
)

java_library(
    name = "org_hibernate_hibernate_annotations",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_slf4j_slf4j_api",
        "@antlr_antlr//jar",
        "@commons_collections_commons_collections//jar",
        "@dom4j_dom4j//jar",
        "@javax_transaction_jta//jar",
        "@org_hibernate_hibernate_annotations//jar",
        "@org_hibernate_hibernate_commons_annotations//jar",
        "@org_hibernate_hibernate_core//jar",
        "@org_hibernate_javax_persistence_hibernate_jpa_2_0_api//jar",
        "@xml_apis_xml_apis//jar",
    ],
)

java_library(
    name = "org_hibernate_hibernate_entitymanager",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:org_slf4j_slf4j_api",
        "@asm_asm//jar",
        "@cglib_cglib//jar",
        "@org_hibernate_hibernate_annotations//jar",
        "@org_hibernate_hibernate_core//jar",
        "@org_hibernate_hibernate_entitymanager//jar",
        "@org_hibernate_javax_persistence_hibernate_jpa_2_0_api//jar",
        "@org_javassist_javassist//jar",
    ],
)

java_library(
    name = "org_hibernate_hibernate_validator",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_classmate",
        "@javax_validation_validation_api//jar",
        "@org_hibernate_hibernate_validator//jar",
        "@org_jboss_logging_jboss_logging//jar",
    ],
)

java_library(
    name = "org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
    visibility = ["//visibility:public"],
    exports = [
        "@org_hibernate_javax_persistence_hibernate_jpa_2_0_api//jar",
    ],
)

java_library(
    name = "org_iban4j_iban4j",
    visibility = ["//visibility:public"],
    exports = [
        "@org_iban4j_iban4j//jar",
    ],
)

java_library(
    name = "org_json_json",
    visibility = ["//visibility:public"],
    exports = [
        "@org_json_json//jar",
    ],
)

java_library(
    name = "org_jsoup_jsoup",
    visibility = ["//visibility:public"],
    exports = [
        "@org_jsoup_jsoup//jar",
    ],
)

java_library(
    name = "org_modelmapper_modelmapper",
    visibility = ["//visibility:public"],
    exports = [
        "@org_modelmapper_modelmapper//jar",
    ],
)

java_library(
    name = "org_mozilla_rhino",
    visibility = ["//visibility:public"],
    exports = [
        "@org_mozilla_rhino//jar",
    ],
)

java_library(
    name = "org_objenesis_objenesis",
    visibility = ["//visibility:public"],
    exports = [
        "@org_objenesis_objenesis//jar",
    ],
)

java_library(
    name = "org_pojava_pojava",
    visibility = ["//visibility:public"],
    exports = [
        "@org_pojava_pojava//jar",
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
        "@org_springframework_data_spring_data_commons//jar",
        "@org_springframework_spring_beans//jar",
        "@org_springframework_spring_core//jar",
    ],
)

java_library(
    name = "org_springframework_data_spring_data_jpa",
    visibility = ["//visibility:public"],
    exports = [
        "@org_springframework_data_spring_data_commons//jar",
        "@org_springframework_data_spring_data_jpa//jar",
        "@org_springframework_spring_aop//jar",
        "@org_springframework_spring_beans//jar",
        "@org_springframework_spring_context//jar",
        "@org_springframework_spring_core//jar",
        "@org_springframework_spring_jdbc//jar",
        "@org_springframework_spring_orm//jar",
        "@org_springframework_spring_tx//jar",
    ],
    runtime_deps = [
        "@aggregation//:org_slf4j_slf4j_api",
        "@org_aspectj_aspectjrt//jar",
    ],
)

java_library(
    name = "org_springframework_security",
    visibility = ["//visibility:public"],
    exports = [
        "@org_springframework_security_core//jar",
    ],
)

java_library(
    name = "org_springframework_spring_core",
    visibility = ["//visibility:public"],
    exports = [
        "@org_springframework_spring_core//jar",
    ],
    runtime_deps = [
        "@aggregation//:commons_logging_commons_logging",
    ],
)

java_library(
    name = "org_springframework_spring_expression",
    visibility = ["//visibility:public"],
    exports = [
        "@org_springframework_spring_expression//jar",
    ],
    runtime_deps = [
        "@org_springframework_spring_core//jar",
    ],
)

java_library(
    name = "org_xerial_snappy_snappy_java",
    visibility = ["//visibility:public"],
    exports = [
        "@org_xerial_snappy_snappy_java//jar",
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
        "@junit_junit//jar",
        "@org_hamcrest_hamcrest_core//jar",
        "@pl_pragmatists_JUnitParams//jar",
    ],
)

java_library(
    name = "org_hamcrest_hamcrest_core",
    visibility = ["//visibility:public"],
    exports = ["@org_hamcrest_hamcrest_core//jar"],
)

java_library(
    # TODO: check is this used?
    name = "org_hamcrest_hamcrest_library",
    visibility = ["//visibility:public"],
    exports = ["@org_hamcrest_hamcrest_library//jar"],
)

java_library(
    name = "org_reflections_reflections",
    visibility = ["//visibility:public"],
    exports = [
        "@org_reflections_reflections//jar",
    ],
    runtime_deps = [
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_slf4j_slf4j_api",
        "@dom4j_dom4j//jar",
        "@javax_servlet_javax_servlet_api//jar",
        "@junit_junit//jar",
        "@org_javassist_javassist//jar",
    ],
)

java_library(
    name = "org_apache_pdfbox_pdfbox",
    visibility = ["//visibility:public"],
    exports = [
        "@org_apache_pdfbox_pdfbox//jar",
    ],
)

java_library(
    name = "org_apache_pdfbox_fontbox",
    visibility = ["//visibility:public"],
    exports = [
        "@org_apache_pdfbox_fontbox//jar",
    ],
)

java_library(
    name = "com_nimbusds_srp6a",
    visibility = ["//visibility:public"],
    exports = [
        "@com_nimbusds_srp6a//jar",
    ],
)

java_library(
    name = "com_nimbusds_nimbus_jose_jwt",
    visibility = ["//visibility:public"],
    exports = [
        "@com_nimbusds_nimbus_jose_jwt//jar",
    ],
    runtime_deps = [
        "@com_github_stephenc_jcip_annotations//jar",
        "@com_google_crypto_tink//jar",
        "@junit_junit//jar",
        "@net_jadler_jadler_all//jar",
        "@net_minidev_json_smart//jar",
        "@org_bitbucket_b_c_jose4j//jar",
        "@org_bouncycastle_bcpkix_jdk15on//jar",
        "@org_bouncycastle_bcprov_jdk15on//jar",
    ],
)

java_library(
    name = "com_auth0_java_jwt",
    visibility = ["//visibility:public"],
    exports = [
        "@com_auth0_java_jwt//jar",
    ],
)

java_library(
    name = "net_sourceforge_tess4j",
    visibility = ["//visibility:public"],
    exports = [
        "@com_sun_media_jai_imageio//jar",
        "@net_java_dev_jna_jna//jar",
        "@net_sourceforge_lept4j//jar",
        "@net_sourceforge_tess4j//jar",
    ],
)

java_library(
    name = "log",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_yaml",
        "@org_apache_logging_log4j_log4j_api//jar",
        "@org_apache_logging_log4j_log4j_core//jar",
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
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_google_guava_guava",
        "@com_google_api_grpc_proto_google_common_protos//jar",
        "@com_google_instrumentation_instrumentation_api//jar",
        "@com_google_protobuf_protobuf_java//jar",
        "@com_google_protobuf_protobuf_java_util//jar",
        "@io_grpc_grpc_api//jar",
        "@io_grpc_grpc_auth//jar",
        "@io_grpc_grpc_context//jar",
        "@io_grpc_grpc_core//jar",
        "@io_grpc_grpc_netty//jar",
        "@io_grpc_grpc_protobuf//jar",
        "@io_grpc_grpc_protobuf_lite//jar",
        "@io_grpc_grpc_protobuf_nano//jar",
        "@io_grpc_grpc_stub//jar",
        "@io_grpc_grpc_testing//jar",
        "@io_netty_netty_buffer//jar",
        "@io_netty_netty_codec//jar",
        "@io_netty_netty_codec_dns//jar",
        "@io_netty_netty_codec_http//jar",
        "@io_netty_netty_codec_http2//jar",
        "@io_netty_netty_codec_socks//jar",
        "@io_netty_netty_common//jar",
        "@io_netty_netty_handler//jar",
        "@io_netty_netty_handler_proxy//jar",
        "@io_netty_netty_resolver//jar",
        "@io_netty_netty_resolver_dns//jar",
        "@io_netty_netty_transport//jar",
        "@io_opencensus_opencensus_api//jar",
        "@io_opencensus_opencensus_contrib_grpc_metrics//jar",
    ],
)

java_library(
    name = "netty-runtime",
    visibility = ["//visibility:public"],
    exports = [
        "@io_netty_netty_tcnative_boringssl_static//jar",
    ],
)

java_library(
    name = "prometheus",
    visibility = ["//visibility:public"],
    exports = [
        "@io_prometheus_simpleclient_common_skeleton_version//jar",
        "@io_prometheus_simpleclient_hotspot_skeleton_version//jar",
        "@io_prometheus_simpleclient_httpserver//jar",
        "@io_prometheus_simpleclient_pushgateway//jar",
        "@io_prometheus_simpleclient_skeleton_version//jar",
    ],
)

java_library(
    name = "zxing-qrcode",
    visibility = ["//visibility:public"],
    exports = [
        "@com_google_zxing_qrcode_core//jar",
        "@com_google_zxing_qrcode_javase//jar",
    ],
)

java_library(
    name = "bouncycastle",
    visibility = ["//visibility:public"],
    exports = [
        "@org_bouncycastle_bcpkix_jdk15on//jar",
        "@org_bouncycastle_bcprov_jdk15on//jar",
    ],
)

java_library(
    name = "io_vavr",
    visibility = ["//visibility:public"],
    exports = [
        "@io_vavr_core//jar",
        "@io_vavr_jackson//jar",
        "@io_vavr_match//jar",
        "@io_vavr_test//jar",
    ],
)

java_library(
    name = "java_spark",
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:commons_logging_commons_logging",
        "@aggregation//:org_slf4j_slf4j_api",
        "@com_sparkjava_spark_core//jar",
        "@javax_servlet_javax_servlet_api_spark_dep//jar",
        "@org_eclipse_jetty_jetty_client_spark_dep//jar",
        "@org_eclipse_jetty_jetty_http_spark_dep//jar",
        "@org_eclipse_jetty_jetty_io_spark_dep//jar",
        "@org_eclipse_jetty_jetty_security_spark_dep//jar",
        "@org_eclipse_jetty_jetty_server_spark_dep//jar",
        "@org_eclipse_jetty_jetty_servlet_spark_dep//jar",
        "@org_eclipse_jetty_jetty_util_spark_dep//jar",
        "@org_eclipse_jetty_jetty_webapp_spark_dep//jar",
        "@org_eclipse_jetty_jetty_xml_spark_dep//jar",
        "@org_eclipse_jetty_websocket_websocket_api_spark_dep//jar",
        "@org_eclipse_jetty_websocket_websocket_client_spark_dep//jar",
        "@org_eclipse_jetty_websocket_websocket_common_spark_dep//jar",
        "@org_eclipse_jetty_websocket_websocket_server_spark_dep//jar",
        "@org_eclipse_jetty_websocket_websocket_servlet_spark_dep//jar",
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
        "@io_grpc_grpc_testing_proto//jar",
    ],
    runtime_deps = [
        "@io_grpc_grpc_protobuf//jar",
        "@io_grpc_grpc_stub//jar",
    ],
)

java_library(
    name = "io_grpc_grpc_netty",
    visibility = ["//visibility:public"],
    exports = [
        "@io_grpc_grpc_netty//jar",
    ],
    runtime_deps = [
        "@io_grpc_grpc_core//jar",
        "@io_netty_netty_codec_http2//jar",
        "@io_netty_netty_handler_proxy//jar",
    ],
)

java_library(
    name = "io_grpc_grpc_context",
    visibility = ["//visibility:public"],
    exports = [
        "@io_grpc_grpc_context//jar",
    ],
)

java_library(
    name = "io_grpc_grpc_stub",
    visibility = ["//visibility:public"],
    exports = [
        "@io_grpc_grpc_stub//jar",
    ],
    runtime_deps = [
        "@io_grpc_grpc_api//jar",
    ],
)

java_library(
    name = "io_grpc_grpc_testing",
    visibility = ["//visibility:public"],
    exports = [
        "@io_grpc_grpc_testing//jar",
    ],
    runtime_deps = [
        "@io_grpc_grpc_core//jar",
        "@io_grpc_grpc_stub//jar",
        "@junit_junit//jar",
    ],
)

java_library(
    name = "io_netty_netty_codec_http2",
    visibility = ["//visibility:public"],
    exports = [
        "@io_netty_netty_codec_http2//jar",
    ],
    runtime_deps = [
        "@aggregation//:com_google_code_gson_gson",
        "@com_jcract_jzlib//jar",
        "@io_netty_netty_buffer//jar",
        "@io_netty_netty_codec//jar",
        "@io_netty_netty_codec_http//jar",
        "@io_netty_netty_common//jar",
        "@io_netty_netty_handler//jar",
        "@io_netty_netty_transport//jar",
        "@org_mockito_mockito_core//jar",
    ],
)

java_library(
    name = "io_netty_netty_common",
    visibility = ["//visibility:public"],
    exports = [
        "@io_netty_netty_common//jar",
    ],
    runtime_deps = [
        "@aggregation//:commons_logging_commons_logging",
        "@aggregation//:org_slf4j_slf4j_api",
        "@com_oracle_substratevm_svm//jar",
        "@io_netty_netty_dev_tools//jar",
        "@log4j_log4j//jar",
        "@org_apache_logging_log4j_log4j_api//jar",
        "@org_apache_logging_log4j_log4j_core//jar",
        "@org_mockito_mockito_core//jar",
    ],
)

java_library(
    name = "io_netty_netty_handler",
    visibility = ["//visibility:public"],
    exports = [
        "@io_netty_netty_handler//jar",
    ],
    runtime_deps = [
        "@io_netty_netty_buffer//jar",
        "@io_netty_netty_codec//jar",
        "@io_netty_netty_tcnative_boringssl_static//jar",
        "@io_netty_netty_transport//jar",
        "@org_bouncycastle_bcpkix_jdk15on//jar",
        "@org_mockito_mockito_core//jar",
    ],
)

java_library(
    name = "io_netty_netty_tcnative_boringssl_static",
    visibility = ["//visibility:public"],
    exports = [
        "@io_netty_netty_tcnative_boringssl_static//jar",
    ],
)

java_library(
    name = "org_mockito_mockito_core",
    visibility = ["//visibility:public"],
    exports = [
        "@org_mockito_mockito_core//jar",
    ],
    runtime_deps = [
        "@net_bytebuddy_byte_buddy//jar",
        "@net_bytebuddy_byte_buddy_agent//jar",
        "@org_objenesis_objenesis//jar",
    ],
)

java_library(
    name = "io_netty_netty_resolver",
    visibility = ["//visibility:public"],
    exports = [
        "@io_netty_netty_resolver//jar",
    ],
    runtime_deps = [
        "@io_netty_netty_common//jar",
        "@org_mockito_mockito_core//jar",
    ],
)

java_library(
    name = "net_minidev_json_smart",
    visibility = ["//visibility:public"],
    exports = [
        "@net_minidev_json_smart//jar",
    ],
    runtime_deps = [
        "@junit_junit//jar",
        "@net_minidev_asm//jar",
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
        "@lombok//:org_projectlombok_lombok",
    ],
)

java_library(
    name = "standalone_agent_libs",
    licenses = ["notice"],
    visibility = ["//visibility:public"],
    exports = [
        "//third_party:com_google_protobuf_protobuf_java",
        "//third_party:lombok_plugin",
        "//third_party:netty",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_base",
        "@aggregation//:com_fasterxml_jackson_module_jackson_module_jaxb_annotations",
        "@aggregation//:org_apache_commons_commons_lang3",
        "@commons_io_commons_io//jar",
        "@io_perfmark_perfmark_api//jar",
        "@lombok//:org_projectlombok_lombok",
        "@org_apache_commons_commons_collections4//jar",
        "@standalone_agent_deps//:ch_qos_logback_logback_classic",
        "@standalone_agent_deps//:ch_qos_logback_logback_core",
        "@standalone_agent_deps//:commons_codec_commons_codec",
        "@standalone_agent_deps//:commons_logging_commons_logging",
        "@standalone_agent_deps//:org_apache_httpcomponents_httpclient",
        "@standalone_agent_deps//:org_apache_httpcomponents_httpcore",
        "@standalone_agent_deps//:org_slf4j_jcl_over_slf4j",
        "@standalone_agent_deps//:org_slf4j_jul_to_slf4j",
        "@standalone_agent_deps//:org_slf4j_slf4j_api",
        "@standalone_agent_deps//:org_springframework_spring_aop",
        "@standalone_agent_deps//:org_springframework_spring_beans",
        "@standalone_agent_deps//:org_springframework_spring_context",
        "@standalone_agent_deps//:org_springframework_spring_core",
        "@standalone_agent_deps//:org_springframework_spring_expression",
        "@standalone_agent_deps//:org_springframework_spring_web",
    ],
)

java_library(
    name = "lombok_libs",
    licenses = ["notice"],
    visibility = ["//visibility:public"],
    exports = [
        "//third_party:lombok_plugin",
        "@lombok//:org_projectlombok_lombok",
    ],
)

java_library(
    name = "standalone_agent_test_libs",
    licenses = ["notice"],
    visibility = ["//visibility:public"],
    exports = [
        "//third_party:standalone_agent_libs",
        "@standalone_agent_deps//:org_springframework_spring_test",
        "@standalone_agent_test_deps//:junit_junit",
        "@standalone_agent_test_deps//:org_mockito_mockito_core",
    ],
)
