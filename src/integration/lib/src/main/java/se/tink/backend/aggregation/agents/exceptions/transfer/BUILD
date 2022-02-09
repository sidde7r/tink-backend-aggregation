load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "transfer",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "//third_party:com_google_guava_guava",
    ],
    intdeps = [
        "//src/libraries/i18n_aggregation",
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation/enums",
    ],
    visibility = ["//visibility:public"],
)
