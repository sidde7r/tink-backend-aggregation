java_library(
    name = "eidas",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
