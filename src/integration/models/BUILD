load("@rules_codeowners//tools:codeowners.bzl", "codeowners", "generate_codeowners")
load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "models",
    srcs = glob(["src/main/**/*.java"]),
    extdeps = [
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_uuid_java_uuid_generator",
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/integration/amount",
        "//src/libraries/serialization",
        "@tink_backend//src/libraries/strings",
        "@tink_backend//src/libraries/uuid",
        "@tink_backend//src/libraries/account_identifier",
    ],
    visibility = ["//visibility:public"],
)

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-agent-platform-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
