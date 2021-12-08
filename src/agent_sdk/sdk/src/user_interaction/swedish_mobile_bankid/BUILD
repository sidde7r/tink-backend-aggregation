load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "swedish_mobile_bankid",
    srcs = glob(["*.java"]),
    extdeps = [
        "@agent_sdk_maven//:com_google_code_findbugs_jsr305",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
    ],
    visibility = ["//visibility:public"],
)
