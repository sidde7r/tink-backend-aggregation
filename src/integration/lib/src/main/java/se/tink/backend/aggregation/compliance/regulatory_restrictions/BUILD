load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "regulatory_restrictions",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/psd2_payment_account/result",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:registry",
        "//third_party:com_google_inject_guice",
        "//third_party:org_slf4j_slf4j_api",

        # JSR305 (javax.annotations)
        "//third_party:com_google_code_findbugs_jsr305",
    ],
)
