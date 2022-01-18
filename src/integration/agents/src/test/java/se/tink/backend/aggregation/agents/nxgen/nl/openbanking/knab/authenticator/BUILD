load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "authenticator",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/knab/resources/responses",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/knab",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/authentication",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filters/iface",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/libraries/agent-storage",
        "//src/libraries/serialization",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)
