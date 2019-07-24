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
        "@maven//:com_google_guava_guava",
        "@maven//:net_spy_spymemcached",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
