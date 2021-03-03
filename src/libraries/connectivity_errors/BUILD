load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "connectivity_errors",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/nemid",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/exceptions/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/connectivity_errors/proto:java_proto",
    ],
)
