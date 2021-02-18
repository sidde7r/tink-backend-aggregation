java_library(
    name = "screenscraping",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/supplementalfields",
        "//src/integration/webdriver/src/main/java/se/tink/integration/webdriver",
        "//src/libraries/serialization",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
    ],
)
