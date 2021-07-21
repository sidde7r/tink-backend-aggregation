load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "converter",
    srcs = glob([
        "*.java",
    ]),
    data = [],
    main_class = "se.tink.backend.aggregation.agents.framework.converter.S3ToAAPFileConverter",
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/utils/parsingrules",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/utils/parsingrules/parsingoperations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/utils/parsingrules/predicates",
        "@aggregation//:com_google_guava_guava",
    ],
)
