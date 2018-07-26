java_library(
    name = "cluster-lib-jersey",
    srcs = glob(["src/main/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/cluster-lib:cluster-lib",
        "//src/libraries/metrics:metrics",
        "//third_party:com_google_guava_guava",
        "//third_party:org_eclipse_jetty_orbit_javax_servlet",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:com_google_inject_guice",
    ]
)
