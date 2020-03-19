load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "qsealc-signer",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:qsealc_crt",
    ],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner/identity",
        "//src/libraries/simple_http_server",
        "//third_party:commons_io_commons_io",
        "//third_party:jetty_server9",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_bouncycastle_bcprov_jdk15on",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_yaml_snakeyaml",
    ],
)
