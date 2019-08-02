java_library(
    name = "tpp-secrets-service-client",
    srcs = glob(
        ["*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//proto/tpp_secrets_service:lib",
        "//src/integration/lib:annotations",
        "//third_party:io_netty_netty_all",
        "//third_party:netty",
        "//third_party:org_slf4j_slf4j_api",
        "@com_fasterxml_jackson_core_jackson_annotations//jar",
        "@com_fasterxml_jackson_core_jackson_databind//jar",
        "@org_bouncycastle_bcprov_jdk15on//jar",
    ],
)
