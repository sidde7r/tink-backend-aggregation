load("@bazel_tools//tools/build_defs/pkg:pkg.bzl", "pkg_tar")
load("@io_bazel_rules_docker//container:container.bzl", "container_image")

java_library(
    name = "service",
    srcs = glob(["src/main/**/*.java"]),
    data = [
        "//data",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib",
        "//src/libraries/draining",
        "//src/libraries/dropwizard_utils",
        "//third_party:io_dropwizard_dropwizard_core",
        "@maven//:com_codahale_metrics_metrics_healthchecks",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:com_netflix_governator_governator",
    ],
)

java_binary(
    name = "bin",
    data = [
        "//data",
        "//etc:development_aggregation_server",
        "//etc:development_minikube_aggregation_server",
    ],
    main_class = "se.tink.backend.aggregation.AggregationServiceContainer",
    visibility = ["//visibility:public"],
    runtime_deps = [
        ":service",
        "//src/libraries/console_json_logger",
        "@maven//:mysql_mysql_connector_java",
        "@maven//:org_hibernate_hibernate_entitymanager",
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
