java_library(
    name = "bancoposta",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe",
        "//src/integration/api",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filterable/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/libraries/i18n",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
    ],
)
