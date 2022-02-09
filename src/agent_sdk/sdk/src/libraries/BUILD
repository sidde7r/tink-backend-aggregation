load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

# Re-export the libraries used by agents.
java_library_with_sources(
    name = "libraries",
    extdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http",
    ],
    intdeps = [
        "//src/libraries/i18n_aggregation",
        "//src/libraries/cryptography",
        "//src/libraries/date",
        "//src/libraries/encoding",
        "//src/libraries/pair",
        "//src/integration/amount",
        "//src/libraries/serialization",
        "@tink_backend//src/libraries/account_identifier",
        "@tink_backend//src/libraries/strings",
    ],
    visibility = [
        "//src/agent_sdk/sdk:__subpackages__",
    ],
    exports = [
        "//src/integration/amount",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http",
        "//src/libraries/cryptography",
        "//src/libraries/date",
        "//src/libraries/encoding",
        "//src/libraries/i18n_aggregation",
        "//src/libraries/pair",
        "//src/libraries/serialization",
        "@tink_backend//src/libraries/account_identifier",
        "@tink_backend//src/libraries/strings",
    ],
)
