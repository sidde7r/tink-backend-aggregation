load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "oauth2based",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/oauth2/constants",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/payloads",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/progressive",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/step",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/authentication",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/libraries/agent-storage",
        "//src/libraries/serialization",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
