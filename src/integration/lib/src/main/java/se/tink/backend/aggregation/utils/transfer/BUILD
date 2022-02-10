load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "transfer",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/transfer",
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation/enums",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//src/libraries/i18n_aggregation",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:com_google_guava_guava",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang3",
    ],
)
