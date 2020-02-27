java_binary(
    name = "converter",
    srcs = glob([
        "*.java",
    ]),
    data = [],
    main_class = "se.tink.backend.aggregation.agents.framework.converter.S3ToAAPFileConverter",
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/wiremock",
        "@aggregation//:com_google_guava_guava",
    ],
)
