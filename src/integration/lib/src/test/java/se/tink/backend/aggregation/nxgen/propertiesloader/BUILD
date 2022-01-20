load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "propertiesloadertest",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen/propertiesloader/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/propertiesloader",
        "//third_party:junit_junit",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
    ],
)

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-minion-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
