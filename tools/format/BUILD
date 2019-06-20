java_binary(
    name = "google-java-format",
    main_class = "com.google.googlejavaformat.java.Main",
    runtime_deps = [
        "@maven//:com_google_errorprone_javac_shaded",
        "@maven//:com_google_googlejavaformat_google_java_format",
        "@maven//:com_google_guava_guava",
    ],
)

sh_binary(
    name = "format-java-diff",
    srcs = [":format-java-diff.sh"],
    data = [":google-java-format"],
    visibility = ["//:__pkg__"],
)
