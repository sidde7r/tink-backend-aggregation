java_library(
    name = "client-lib",
    srcs = glob(["src/main/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_guava_guava",
        "//src/libraries/metrics:metrics",
        "//third_party:com_google_inject_guice",
        "//:main-api",
        "//third_party:com_sun_jersey_jersey_server",  
    ]
)
