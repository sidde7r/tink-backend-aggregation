java_library(
    name = "sdcno",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/signaturekeypair",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/bankid/screenscraping",
        "//src/libraries/selenium",
        "@aggregation//:com_google_guava_guava",
        "@selenium//:com_codeborne_phantomjsdriver",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
    ],
)
