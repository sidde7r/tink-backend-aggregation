load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/enums",
        "//src/libraries/serialization",
        "//src/libraries/uuid",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
    ],
)

load("@rules_codeowners//tools:codeowners.bzl", "codeowners", "generate_codeowners")

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-agent-platform-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
