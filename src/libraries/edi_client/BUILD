java_binary(
    name = "issue",
    srcs = glob(["src/main/java/**/*.java"]),
    main_class = "se.tink.ediclient.EdiMain",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "@aggregation//:org_slf4j_slf4j_simple",
    ],
    deps = [
        "//third_party:org_slf4j_slf4j_api",
        "@tink_backend//src/eidas-dev-issuer/client",
    ],
)
