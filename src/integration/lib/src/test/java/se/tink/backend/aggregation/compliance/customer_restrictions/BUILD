load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "customer_restrictions",
    srcs = glob(["*.java"]),
    deps = [
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/customer_restrictions",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
