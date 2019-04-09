java_binary(
    name = "google-java-format",
    main_class = "com.google.googlejavaformat.java.Main",
    runtime_deps = [
        "@com_google_errorprone//jar",
        "@com_google_googlejavaformat//jar",
        "@com_google_guava_guava//jar",
    ],
)

sh_binary(
    name = "format-java-diff",
    srcs = [":format-java-diff.sh"],
    data = [":google-java-format"],
    visibility = ["//:__pkg__"],
)
