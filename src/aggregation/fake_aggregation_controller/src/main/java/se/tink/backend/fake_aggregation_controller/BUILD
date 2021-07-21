load("@rules_pkg//:pkg.bzl", "pkg_tar")
load("@io_bazel_rules_docker//container:container.bzl", "container_image")

java_library(
    name = "fake_aggregation_controller",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/fake_aggregation_controller/src/main/java/se/tink/backend/fake_aggregation_controller/dto",
        "//src/aggregation/fake_aggregation_controller/src/main/java/se/tink/backend/fake_aggregation_controller/state_controller",
        "//third_party:com_codahale_metrics_metrics_healthchecks",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_jersey",
        "//third_party:lombok_plugin",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:javax_xml_bind_jaxb_api",
        "@aggregation//:org_glassfish_jaxb_jaxb_runtime",
        "@aggregation//:org_projectlombok_lombok",
    ],
)

java_binary(
    name = "bin",
    main_class = "se.tink.backend.fake_aggregation_controller.FakeAggregationController",
    visibility = ["//visibility:public"],
    runtime_deps = [
        ":fake_aggregation_controller",
    ],
)

pkg_tar(
    name = "bin_tar",
    srcs = [":bin_deploy"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-aggregation",
    tags = ["manual"],
    visibility = ["//docker:__pkg__"],
)

genrule(
    name = "bin_deploy",
    # Implicitly built, see: https://docs.bazel.build/versions/master/be/java.html#java_binary_implicit_outputs
    srcs = [":bin_deploy.jar"],
    outs = ["fake_aggregation_controller.jar"],
    cmd = "cp $(location :bin_deploy.jar) \"$(@)\"",
    tags = ["manual"],
    visibility = ["//visibility:public"],
)

container_image(
    name = "image",
    base = "@openjdk_jdk8//image",
    cmd = [
        "java",
        "-jar",
        "fake_aggregation_controller.jar",
    ],
    tags = ["manual"],
    tars = [
        ":bin_tar",
    ],
    visibility = ["//visibility:public"],
    workdir = "/usr/share/tink-backend-aggregation",
)
