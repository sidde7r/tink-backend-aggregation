java_library(
    name = "http-utils",
    srcs = glob(["src/main/java/se/tink/libraries/http/utils/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/log",
        "//third_party:com_sun_jersey_jersey_core",
    ],
)

java_library(
    name = "http-annotations",
    srcs = glob(["src/main/java/se/tink/libraries/http/annotations/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_guava_guava",
        "//third_party:javax_validation_validation_api",
    ],
)
