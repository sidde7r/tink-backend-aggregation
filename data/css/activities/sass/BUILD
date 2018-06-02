load("@io_bazel_rules_sass//sass:sass.bzl", "sass_library", "sass_binary")

sass_library(
    name = "lib",
    srcs = glob(["_*.scss"]),
)

sass_binary(
    name = "tink",
    src = "styles.tink.scss",
    output_style = "expanded",
    deps = [":lib"],
    visibility = ["//visibility:public"],
)

sass_binary(
    name = "tink-ios",
    src = "styles.tink-ios.scss",
    output_style = "expanded",
    deps = [":lib"],
    visibility = ["//visibility:public"],
)

sass_binary(
    name = "tink-android",
    src = "styles.tink-android.scss",
    output_style = "expanded",
    deps = [":lib"],
    visibility = ["//visibility:public"],
)

sass_binary(
    name = "tink-ios-v2",
    src = "styles.tink-ios-v2.scss",
    output_style = "expanded",
    deps = [":lib"],
    visibility = ["//visibility:public"],
)

sass_binary(
    name = "tink-android-v2",
    src = "styles.tink-android-v2.scss",
    output_style = "expanded",
    deps = [":lib"],
    visibility = ["//visibility:public"],
)

sass_binary(
    name = "abnamro",
    src = "styles.abnamro.scss",
    output_style = "expanded",
    deps = [":lib"],
    visibility = ["//visibility:public"],
)

sass_binary(
    name = "abnamro-ios",
    src = "styles.abnamro-ios.scss",
    output_style = "expanded",
    deps = [":lib"],
    visibility = ["//visibility:public"],
)

sass_binary(
    name = "abnamro-android",
    src = "styles.abnamro-android.scss",
    output_style = "expanded",
    deps = [":lib"],
    visibility = ["//visibility:public"],
)

sass_binary(
    name = "abnamro-ios-v2",
    src = "styles.abnamro-ios-v2.scss",
    output_style = "expanded",
    deps = [":lib"],
    visibility = ["//visibility:public"],
)

sass_binary(
    name = "abnamro-android-v2",
    src = "styles.abnamro-android-v2.scss",
    output_style = "expanded",
    deps = [":lib"],
    visibility = ["//visibility:public"],
)
