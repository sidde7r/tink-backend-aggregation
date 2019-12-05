load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "qsealc-signer",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:qsealc_crt",
    ],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner",
        "//src/libraries/simple_http_server",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:jetty_server9",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_bouncycastle_bcprov_jdk15on",
        "//third_party:org_yaml_snakeyaml",
    ],
)
