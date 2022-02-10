load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "exceptions",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "//third_party:com_google_guava_guava",
        "//third_party:lombok_libs",
        "@tink_backend//src/libraries/connectivity_errors/proto:java_proto",
    ],
    intdeps = [
        "//src/libraries/i18n_aggregation",
        "@tink_backend//src/libraries/account_identifier",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/transfer",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation/enums",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/transfer",
    ],
)
