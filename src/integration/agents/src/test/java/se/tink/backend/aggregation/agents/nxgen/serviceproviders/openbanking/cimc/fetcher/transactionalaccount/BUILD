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
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cmcic",
        "//src/integration/identity_data",
        "//src/integration/lib",
        "//third_party:org_hamcrest_hamcrest_library",
        "//third_party:org_mockito_mockito_core",
    ],
)
