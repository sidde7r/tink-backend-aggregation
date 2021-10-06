load("@rules_codeowners//tools:codeowners.bzl", "codeowners")

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-nazguls-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

filegroup(
    name = "resources",
    srcs = glob(
        include = ["*"],
        exclude = [
            "BUILD",
        ],
    ),
    visibility = ["//visibility:public"],
)
