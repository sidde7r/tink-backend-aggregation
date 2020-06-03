load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "demofinancialinstitution",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/demo/banks/demofinancialinstitution",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//third_party:org_hamcrest_hamcrest_library",
        "@aggregation//:com_google_guava_guava",
    ],
)
