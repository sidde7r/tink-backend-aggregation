load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "command", "multirun")

multirun(
    name = "fmt",
    commands = [
        "//tools/format/java:format-java-diff",
        "//tools/format:format-json-diff",
        "//tools/format/bazel:format-build-files",
    ],
    parallel = True,
    visibility = ["//:__pkg__"],
)

sh_binary(
    name = "format-json-diff",
    srcs = [":format-json-diff.sh"],
    data = [":jsonformat.py"],
    visibility = ["//:__pkg__"],
)
