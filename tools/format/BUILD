load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "command", "multirun")
load("@rules_java//java:defs.bzl", "java_binary")

multirun(
    name = "fmt",
    commands = [
        "//tools/format:format-java-diff",
        "//tools/format:format-json-diff",
        "@tink_backend//tools/format/bazel:format-build-files",
    ],
    parallel = True,
    visibility = ["//:__pkg__"],
)

java_binary(
    name = "google-java-format",
    main_class = "com.google.googlejavaformat.java.Main",
    runtime_deps = [
        "@java_formatter//:com_google_errorprone_javac_shaded",
        "@java_formatter//:com_google_googlejavaformat_google_java_format",
        "@java_formatter//:com_google_guava_guava",
    ],
)

sh_binary(
    name = "format-java-diff",
    srcs = [":format-java-diff.sh"],
    data = [":google-java-format"],
    visibility = ["//:__pkg__"],
)

sh_binary(
    name = "format-json-diff",
    srcs = [":format-json-diff.sh"],
    data = [":jsonformat.py"],
    visibility = ["//:__pkg__"],
)
