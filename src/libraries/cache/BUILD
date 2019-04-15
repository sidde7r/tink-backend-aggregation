load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "cache",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.cache",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/concurrency",
        "//src/libraries/executor_utils",
        "//src/libraries/metrics",
        "//third_party:com_google_guava_guava",
        "//third_party:net_spy_spymemcached",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
