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
