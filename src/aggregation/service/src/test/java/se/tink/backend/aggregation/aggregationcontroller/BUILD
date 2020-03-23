java_library(
    name = "fake_aggregationcontroller",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_jersey",
        "//third_party:org_slf4j_slf4j_api",
    ],
)

java_binary(
    name = "fake_aggregationcontroller_bin",
    main_class = "se.tink.backend.aggregation.aggregationcontroller.FakeAggregationController",
    visibility = ["//visibility:public"],
    runtime_deps = [
        ":fake_aggregationcontroller",
    ],
)
