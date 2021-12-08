load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "agent-storage",
    srcs = glob(["*.java"]),
    extdeps = [
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:io_reactivex_rxjava3_rxjava",
    ],
    intdeps = [
        "//src/libraries/serialization",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/log",
    ],
    visibility = ["//visibility:public"],
)
