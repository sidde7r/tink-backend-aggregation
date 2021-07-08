java_library(
    name = "state_controller",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/fake_aggregation_controller/src/main/java/se/tink/backend/fake_aggregation_controller/dto",
        "//third_party:com_codahale_metrics_metrics_healthchecks",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_jersey",
        "//third_party:lombok_plugin",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:org_projectlombok_lombok",
    ],
)
