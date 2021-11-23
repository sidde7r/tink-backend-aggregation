load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "signature",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/module/annotation",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents/utils",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner/module",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/request",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:javax_validation_validation_api",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_apache_commons_commons_lang3",
    ],
)

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-fluffy-maintainer",
        "@tink-ab/aggregation-thundercats-maintainer",
        "@tink-ab/aggregation-minion-maintainer",
        "@tink-ab/aggregation-nazguls-maintainer",
        "@tink-ab/aggregation-teletubbies-maintainer",
        "@tink-ab/aggregation-llamas-maintainer",
        "@tink-ab/aggregation-penguins-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
