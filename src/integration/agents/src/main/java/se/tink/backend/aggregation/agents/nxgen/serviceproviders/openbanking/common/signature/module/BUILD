load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "module",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/common/signature",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/module/annotation",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner/module",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/request",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
