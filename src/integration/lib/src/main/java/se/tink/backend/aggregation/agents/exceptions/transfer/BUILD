load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "transfer",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation/enums",
        "//src/libraries/i18n",
        "//third_party:com_google_guava_guava",
    ],
)
