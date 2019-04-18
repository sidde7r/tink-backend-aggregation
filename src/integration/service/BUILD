load("@bazel_tools//tools/build_defs/pkg:pkg.bzl", "pkg_tar")
load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_image",
    "container_push",
)

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
        "//third_party:log",
        "@maven//:io_netty_netty_tcnative_boringssl_static",
    ],
    deps = [
        "//src/integration/grpc-server",
        "//src/integration/grpc-services",
        "//src/libraries/metrics",
        "//third_party:log",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:io_grpc_grpc_core",
        "@maven//:io_prometheus_simpleclient_hotspot",
        "@maven//:org_yaml_snakeyaml",
        "@tink_backend_shared_libraries//libraries/health-check-handler",
        "@tink_backend_shared_libraries//libraries/simple-http-server",
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
    base = "@openjdk_jdk8//image",
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
