java_library(
    name = "net_sourceforge_tess4j_tess4j",
    visibility = ["//visibility:public"],
    exports = [
        "@com_sun_media_jai_imageio//jar",
        "@net_java_dev_jna_jna//jar",
        "@net_sourceforge_lept4j_lept4j//jar",
        "@net_sourceforge_tess4j_tess4j//jar",
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
    name = "log",
    visibility = ["//visibility:public"],
    exports = [
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_fasterxml_jackson_dataformat_jackson_dataformat_yaml",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_yaml_snakeyaml",
    ],
)

java_library(
    name = "io_vavr",
    visibility = ["//visibility:public"],
    exports = [
        "@maven//:io_vavr_vavr",
        "@maven//:io_vavr_vavr_jackson",
        "@maven//:io_vavr_vavr_match",
        "@maven//:io_vavr_vavr_test",
    ],
)

java_import(
    name = "com_amazonaws_aws_java_sdk_sqs",
    jars = [
        # This should be a java_librar w/ exports instead, see:
        # https://github.com/bazelbuild/intellij/issues/66
        "@tink_aws_sdk_1_11//:libaws-java-sdk-core.jar",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "@com_amazonaws_aws_java_sdk_code_generator//jar",
        "@com_amazonaws_aws_java_sdk_core//jar",
        "@com_amazonaws_aws_java_sdk_sqs//jar",
        "@com_amazonaws_jmespath_java//jar",
        "@tink_httpclient_4_5_5",
        "@tink_httpcore_4_4_9",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_client",
    visibility = ["//visibility:public"],
    exports = [
        "@com_codahale_metrics_metrics_core//jar",
        "@com_codahale_metrics_metrics_httpclient//jar",
        "@com_sun_jersey_contribs_jersey_apache_client4//jar",
        "@com_sun_jersey_jersey_client//jar",
        "@commons_codec_commons_codec//jar",
        "@io_dropwizard_dropwizard_client//jar",
        "@io_dropwizard_dropwizard_core//jar",
        "@org_apache_httpcomponents_httpclient//jar",
        "@org_apache_httpcomponents_httpcore//jar",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_configuration",
    visibility = ["//visibility:public"],
    exports = [
        "@com_fasterxml_jackson_core_jackson_core//jar",
        "@com_fasterxml_jackson_dataformat_jackson_dataformat_yaml//jar",
        "@commons_lang_commons_lang//jar",
        "@io_dropwizard_dropwizard_configuration//jar",
        "@io_dropwizard_dropwizard_jackson//jar",
        "@io_dropwizard_dropwizard_validation//jar",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_core",
    visibility = ["//visibility:public"],
    exports = [
        ":io_dropwizard_dropwizard_jersey",
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
        "@com_fasterxml_classmate//jar",
        "@com_fasterxml_jackson_core_jackson_annotations//jar",
        "@com_fasterxml_jackson_core_jackson_core//jar",
        "@com_fasterxml_jackson_core_jackson_databind//jar",
        "@com_fasterxml_jackson_dataformat_jackson_dataformat_yaml//jar",
        "@com_fasterxml_jackson_datatype_jackson_datatype_guava//jar",
        "@com_fasterxml_jackson_datatype_jackson_datatype_joda//jar",
        "@com_fasterxml_jackson_jaxrs_jackson_jaxrs_base//jar",
        "@com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider//jar",
        "@com_fasterxml_jackson_module_jackson_module_afterburner//jar",
        "@com_fasterxml_jackson_module_jackson_module_jaxb_annotations//jar",
        "@com_google_code_findbugs_jsr305//jar",
        "@com_google_guava_guava//jar",
        "@com_sun_jersey_jersey_core//jar",
        "@com_sun_jersey_jersey_server//jar",
        "@com_sun_jersey_jersey_servlet//jar",
        "@commons_lang_commons_lang//jar",
        "@io_dropwizard_dropwizard_configuration//jar",
        "@io_dropwizard_dropwizard_core//jar",
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
        "@org_slf4j_slf4j_api//jar",
        "@org_yaml_snakeyaml//jar",
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
        "@com_codahale_metrics_metrics_annotation//jar",
        "@com_codahale_metrics_metrics_core//jar",
        "@com_codahale_metrics_metrics_jersey//jar",
        "@com_fasterxml_jackson_jaxrs_jackson_jaxrs_base//jar",
        "@com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider//jar",
        "@com_fasterxml_jackson_module_jackson_module_jaxb_annotations//jar",
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
        "@com_google_guava_guava//jar",
        "@io_dropwizard_dropwizard_lifecycle//jar",
        "@io_dropwizard_dropwizard_util//jar",
        "@org_eclipse_jetty_jetty_http//jar",
        "@org_eclipse_jetty_jetty_io//jar",
        "@org_eclipse_jetty_jetty_server//jar",
        "@org_eclipse_jetty_jetty_util//jar",
        "@org_eclipse_jetty_orbit_javax_servlet//jar",
        "@org_slf4j_slf4j_api//jar",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_logging",
    visibility = ["//visibility:public"],
    exports = [
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
        "@org_slf4j_slf4j_api//jar",
    ],
)

java_library(
    name = "io_dropwizard_dropwizard_metrics",
    visibility = ["//visibility:public"],
    exports = [
        "@com_codahale_metrics_metrics_core//jar",
        "@com_google_guava_guava//jar",
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
        "@org_slf4j_slf4j_api//jar",
    ],
)
