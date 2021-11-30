load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "enums",
    srcs = glob(["src/main/**/*.java"]),
    extdeps = [
        "//third_party:org_apache_commons_commons_lang3",
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "@tink_backend//src/libraries/account_identifier",
    ],
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller:__subpackages__",
        "//src/integration:__subpackages__",
    ],
)

codeowners(
    name = "integration_enums_owners",
    teams = [
        "@tink-ab/aggregation-agent-platform-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
