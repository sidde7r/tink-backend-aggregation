java_library(
    name = "metrics",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",

        "//third_party:prometheus",
        "//third_party:io_prometheus_simpleclient_servlet",

        "//third_party:org_slf4j_slf4j_api",
        "//third_party:com_google_code_findbugs_jsr305",

        "//third_party:org_eclipse_jetty_orbit_javax_servlet",

        "@org_eclipse_jetty_jetty_client_spark_dep//jar",
        "@org_eclipse_jetty_jetty_http_spark_dep//jar",
        "@org_eclipse_jetty_jetty_io_spark_dep//jar",
        "@org_eclipse_jetty_jetty_security_spark_dep//jar",
        "@org_eclipse_jetty_jetty_server_spark_dep//jar",
        "@org_eclipse_jetty_jetty_servlet_spark_dep//jar",
        "@org_eclipse_jetty_jetty_util_spark_dep//jar",
        "@org_eclipse_jetty_jetty_webapp_spark_dep//jar",
        "@org_eclipse_jetty_jetty_xml_spark_dep//jar",
    ],
)
