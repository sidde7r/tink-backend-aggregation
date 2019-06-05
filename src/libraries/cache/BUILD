java_library(
    name = "cache",
    srcs = glob(["src/main/**/*.java"]),
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
