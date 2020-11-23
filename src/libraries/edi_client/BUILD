load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "edi_client",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_slf4j_slf4j_api",
    ],
)

java_binary(
    name = "issue",
    main_class = "se.tink.ediclient.EdiMain",
    runtime_deps = [
        ":edi_client",
        "@aggregation//:org_slf4j_slf4j_simple",
    ],
)

junit_test(
    name = "edi_client_test",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["manual"],
    visibility = ["//visibility:public"],
    deps = [
        ":edi_client",
        "//third_party:junit_junit",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
