java_library(
    name = "customsupplemental",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/client/provider_configuration/rpc",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/libraries/serialization",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
    ],
)
