load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "boursorama",
    srcs = glob(
        ["**/*.java"],
        exclude = ["agent/**/*.java"],
    ),
    tags = [
        "external",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/boursorama",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filters/iface",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
