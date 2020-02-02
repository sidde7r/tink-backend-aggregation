load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "transactionalaccount",
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
        "//src/integration/amount",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/libraries/account_identifier",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:org_hamcrest_hamcrest_library",
        "//third_party:org_mockito_mockito_core",
    ],
)
