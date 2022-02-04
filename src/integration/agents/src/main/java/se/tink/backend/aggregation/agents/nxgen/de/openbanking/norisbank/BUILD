load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "norisbank",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/deutschebank",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/enums",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup/payment",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/payment/validation/impl",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//third_party:com_google_inject_guice",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
