load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "configuration",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//etc:development",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:asm_asm",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/creditagricole",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas/proxy",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/configuration/iface",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//third_party:org_hamcrest_hamcrest_library",
        "//third_party:org_mockito_mockito_core",
    ],
)
