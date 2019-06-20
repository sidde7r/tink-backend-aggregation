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
        "@tink_aws_sdk//:com_amazonaws_aws_java_sdk_code_generator",
        "@tink_aws_sdk//:com_amazonaws_aws_java_sdk_core",
        "@tink_aws_sdk//:com_amazonaws_aws_java_sdk_sqs",
        "@tink_aws_sdk//:com_amazonaws_jmespath_java",
        "@tink_httpclient_4_5_5",
        "@tink_httpcore_4_4_9",
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
        "@dropwizard//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_base",
        "@dropwizard//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
        "@dropwizard//:com_fasterxml_jackson_module_jackson_module_jaxb_annotations",
        "@dropwizard//:io_dropwizard_dropwizard_jackson",
        "@dropwizard//:io_dropwizard_dropwizard_logging",
        "@dropwizard//:io_dropwizard_dropwizard_validation",
        "@other//:com_codahale_metrics_metrics_annotation",
        "@other//:com_codahale_metrics_metrics_core",
        "@other//:com_codahale_metrics_metrics_jersey",
        "@other//:com_sun_jersey_jersey_core",
        "@other//:com_sun_jersey_jersey_server",
        "@other//:com_sun_jersey_jersey_servlet",
        "@other//:org_eclipse_jetty_orbit_javax_servlet",
    ],
)
