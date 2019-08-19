# This is an inline version of tink-ab/aws-sdk-java as of
# 1bd88709966b245373b4b71f5bca4c0d7202bf1a
# It's based on 863a7e6f3699fc139af8e9ee69b74e6d75072fa7 from aws/aws-sdk-java
java_library(
    name = "aws_java_sdk_core",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@com_fasterxml_jackson_core_jackson_annotations//jar",
        "@com_fasterxml_jackson_core_jackson_core//jar",
        "@com_fasterxml_jackson_core_jackson_databind//jar",
        "@com_fasterxml_jackson_dataformat_jackson_dataformat_cbor//jar",
        "@commons_logging_commons_logging//jar",
        "@joda_time_joda_time//jar",
        "@software_amazon_ion_ion_java//jar",
        "@tink_httpclient_4_5_5",
        "@tink_httpcore_4_4_9",
    ],
)
