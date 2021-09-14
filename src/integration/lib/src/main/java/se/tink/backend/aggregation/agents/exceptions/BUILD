load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "exceptions",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/transfer",
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation/enums",
        "//src/libraries/i18n",
        "//third_party:com_google_guava_guava",
        "//third_party:lombok_libs",
        "@tink_backend//src/libraries/account_identifier",
        "@tink_backend//src/libraries/connectivity_errors/proto:java_proto",
    ],
)
