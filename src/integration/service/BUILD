load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_image",
    "container_push",
)
load("@rules_java//java:defs.bzl", "java_binary")
load("@rules_pkg//:pkg.bzl", "pkg_tar")

java_binary(
    name = "integration",
    srcs = glob(["src/main/java/**/*.java"]),
    data = [
        "//etc/integration:development-configuration",
    ],
    main_class = "se.tink.backend.integration.boot.IntegrationService",
    resources = glob([
        "src/main/resources/**/*",
    ]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:log-runtime",
        "//third_party:netty-runtime",
    ],
    deps = [
        "//src/integration/grpc-server",
        "//src/libraries/health_check_handler",
        "//src/libraries/metrics:collection",
        "//src/libraries/metrics:configuration",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:counters",
        "//src/libraries/metrics:gauges",
        "//src/libraries/metrics:histograms",
        "//src/libraries/metrics:logbackappender",
        "//src/libraries/metrics:monitor",
        "//src/libraries/metrics:other",
        "//src/libraries/metrics:prometheus",
        "//src/libraries/metrics:registry",
        "//src/libraries/metrics:timers",
        "//src/libraries/simple_http_server",
        "//third_party:com_google_inject_guice",
        "//third_party:io_grpc_grpc_core",
        "//third_party:log",
        "//third_party:prometheus",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_yaml_snakeyaml",
    ],
)

genrule(
    name = "renamed-deploy-jar",
    srcs = [
        ":integration_deploy.jar",
    ],
    outs = [
        "service.jar",
    ],
    cmd = "cp $(location :integration_deploy.jar) \"$(@)\"",
    visibility = ["//visibility:public"],
)

pkg_tar(
    name = "bin_tar",
    srcs = [":renamed-deploy-jar"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-integration",
    tags = ["manual"],
)

container_image(
    name = "docker",
    base = "@distroless_java_11//image",
    tags = ["manual"],
    tars = [
        ":bin_tar",
    ],
    workdir = "/usr/share/tink-backend-integration",
)

container_push(
    name = "docker_push",
    format = "Docker",
    image = ":docker",
    registry = "gcr.io",
    repository = "tink-containers/tink-backend-integration",
    tag = "{TINK_VERSION}",
    tags = ["manual"],
)
