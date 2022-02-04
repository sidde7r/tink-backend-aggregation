load("//tools/bzl:junit5.bzl", "junit5_test")

junit5_test(
    name = "test",
    srcs = glob(["**/*.java"]),
    test_package = "se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.test",
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/test",
        "//third_party:org_assertj_assertj_core",
    ],
)
