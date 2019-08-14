load("@bazel_tools//tools/build_defs/pkg:pkg.bzl", "pkg_tar")
load("@io_bazel_rules_docker//container:container.bzl", "container_image")
load("//tools/bzl:template_file.bzl", "templated_file")

java_library(
    name = "service",
    srcs = glob(["src/main/**/*.java"]),
    data = [
        "//data",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib",
        "//src/integration/agent_data_availability_tracker/client",
        "//src/libraries/draining",
        "//src/libraries/dropwizard_utils",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_netflix_governator",
        "//third_party:io_dropwizard_dropwizard_core",
    ],
)

java_binary(
    name = "bin",
    args = [
        "server",
        "$(location :development_config.out)",
    ],
    data = [
        ":development_config.out",
        "//data",
    ],
    main_class = "se.tink.backend.aggregation.AggregationServiceContainer",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/aggregation/service",
        "//src/libraries/console_json_logger",
        "//third_party:mysql_mysql_connector_java",
    ],
)

genrule(
    name = "bin_deploy",
    # Implicitly built, see: https://docs.bazel.build/versions/master/be/java.html#java_binary_implicit_outputs
    srcs = [":bin_deploy.jar"],
    outs = ["aggregation_service.jar"],
    cmd = "cp $(location :bin_deploy.jar) \"$(@)\"",
    tags = ["manual"],
    visibility = ["//visibility:public"],
)

container_image(
    name = "aggregation_debug_image",
    base = "@openjdk_jdk8//image",
    cmd = [
        "java",
        "-jar",
        "-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n",
        "aggregation_service.jar",
        "server",
        "development-minikube-aggregation-server.yml",
    ],
    tags = ["manual"],
    tars = [
        ":bin_tar",
        ":data_tar",
    ],
    visibility = ["//visibility:public"],
    workdir = "/usr/share/tink-backend-aggregation",
)

templated_file(
    name = "development_config",
    template = "development-minikube.yaml",
)

pkg_tar(
    name = "data_tar",
    srcs = ["//data"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-aggregation/data",
    strip_prefix = "/data",
    tags = ["manual"],
    visibility = ["//docker:__pkg__"],
)

pkg_tar(
    name = "bin_tar",
    srcs = [":bin_deploy"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-aggregation",
    tags = ["manual"],
    visibility = ["//docker:__pkg__"],
)
