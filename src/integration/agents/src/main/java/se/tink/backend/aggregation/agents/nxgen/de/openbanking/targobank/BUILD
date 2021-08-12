load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("@rules_java//java:defs.bzl", "java_library")

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-nazguls-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

java_library(
    name = "targobank",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/client/provider_configuration/rpc",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bankverlag",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/agents/componentproviders",
        "//third_party:com_google_inject_guice",
    ],
)
