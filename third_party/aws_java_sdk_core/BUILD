load("@rules_java//java:defs.bzl", "java_library")

# This is an inline version of tink-ab/aws-sdk-java as of
# 1bd88709966b245373b4b71f5bca4c0d7202bf1a
# It's based on 863a7e6f3699fc139af8e9ee69b74e6d75072fa7 from aws/aws-sdk-java
java_library(
    name = "aws_java_sdk_core",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = [
        "aws_java_sdk_core_resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_cbor",
        "@aggregation//:com_sun_xml_ws_jaxws_rt",
        "@aggregation//:commons_logging_commons_logging",
        "@aggregation//:jakarta_xml_bind_jakarta_xml_bind_api",
        "@aggregation//:jakarta_xml_soap_jakarta_xml_soap_api",
        "@aggregation//:jakarta_xml_ws_jakarta_xml_ws_api",
        "@aggregation//:joda_time_joda_time",
        "@aggregation//:software_amazon_ion_ion_java",
        "@tink_httpclient_4_5_5",
        "@tink_httpcore_4_4_9",
    ],
)

filegroup(
    name = "aws_java_sdk_core_resources",
    srcs = glob(["src/main/resources/com/amazonaws/**/*"]),
    visibility = ["//visibility:public"],
)
