load("@rules_codeowners//tools:codeowners.bzl", "codeowners")

filegroup(
    name = "phantomjs_linux",
    srcs = ["phantomjs-tink-linux-x86_64-2.1.1"],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "phantomjs_mac",
    srcs = ["phantomjs-tink-mac64-2.1.1"],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "libkbc_wbaes_linux",
    srcs = ["libkbc_wbaes_linux.so"],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "libkbc_wbaes_mac",
    srcs = ["libkbc_wbaes_mac.dylib"],
    visibility = ["//visibility:public"],
)

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-fluffy-maintainer",
        "@tink-ab/aggregation-thundercats-maintainer",
        "@tink-ab/aggregation-minion-maintainer",
        "@tink-ab/aggregation-nazguls-maintainer",
        "@tink-ab/aggregation-teletubbies-maintainer",
        "@tink-ab/aggregation-llamas-maintainer",
        "@tink-ab/aggregation-penguins-maintainer",
        "@tink-ab/aggregation-toros-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
