load("@rules_pkg//:pkg.bzl", "pkg_tar")
load("@io_bazel_rules_docker//container:container.bzl", "container_image")
load("//tools/bzl:template_file.bzl", "templated_file")
load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "command", "multirun")

java_library(
    name = "service",
    srcs = glob(["src/main/**/*.java"]),
    data = [
        "//data",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/cli",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/configuration",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/configuration/guice/modules",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/daos",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/worker",
        "//src/integration/agent_data_availability_tracker/client",
        "//src/integration/agent_data_availability_tracker/client:module",
        "//src/integration/tpp_secrets_service/client",
        "//src/libraries/draining",
        "//src/libraries/dropwizard_utils",
        "//third_party:com_codahale_metrics_metrics_healthchecks",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_core",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_netflix_governator_governator",
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
        "@aggregation//:mysql_mysql_connector_java",
    ],
)

java_binary(
    name = "bin_decoupled",
    args = [
        "server",
        "$(location :decoupled_config.out)",
    ],
    data = [
        ":decoupled_config.out",
        "//data",
    ],
    main_class = "se.tink.backend.aggregation.AggregationServiceContainer",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/aggregation/service",
        "//src/libraries/console_json_logger",
        "@aggregation//:mysql_mysql_connector_java",
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

container_image(
    name = "aggregation_decoupled_image",
    base = "@openjdk_jdk8//image",
    cmd = [
        "java",
        "-jar",
        "aggregation_service.jar",
        "server",
        "development-decoupled.yaml",
    ],
    tags = ["manual"],
    tars = [
        ":bin_tar",
        ":data_tar",
        ":config_tar",
    ],
    visibility = ["//visibility:public"],
    workdir = "/usr/share/tink-backend-aggregation",
)

templated_file(
    name = "development_config",
    template = "development-minikube.yaml",
)

templated_file(
    name = "decoupled_config",
    template = "development-decoupled.yaml",
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

pkg_tar(
    name = "config_tar",
    srcs = [":development-decoupled.yaml"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-aggregation",
    tags = ["manual"],
    visibility = ["//docker:__pkg__"],
)

command(
    name = "run_aggregation_decoupled",
    arguments = [
        "server",
        "src/aggregation/service/decoupled_config.out",
    ],
    command = "//src/aggregation/service:bin_decoupled",
)

command(
    name = "run_fake_aggregation_controller",
    arguments = [
        "--debug_mode",
    ],
    command = "//src/aggregation/service/src/test/java/se/tink/backend/aggregation/aggregationcontroller:fake_aggregationcontroller_bin",
)

multirun(
    name = "run_aggregation_decoupled_and_external_deps",
    commands = [
        "//src/aggregation/fakebank/src/main/java/se/tink/backend/fakebank",
        ":run_fake_aggregation_controller",
        ":run_aggregation_decoupled",
    ],
    parallel = True,
)
