load("@bazel_tools//tools/build_defs/pkg:pkg.bzl", "pkg_tar")
load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_image",
    "container_push",
)

java_binary(
    name = "integration",
    srcs = glob(["src/main/java/**/*.java"]),
    main_class = "se.tink.backend.integration.boot.IntegrationService",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:log-runtime",
    ],
    resources = glob([
        "src/main/resources/**/*",
    ]),
    data = [
        "//etc/integration:development-configuration",
    ],
    deps = [
        "//src/integration/grpc-server:grpc-server",
        "//src/integration/ping-service:ping-service",

        "//src/libraries:simple-http-server",
        "//src/libraries:health-check-handler",

        "//third_party:log",
        "//third_party:prometheus",

        "@io_grpc_grpc_core//jar",
        "//third_party:com_google_guava_guava",
        "//third_party:org_yaml_snakeyaml",
    ],
)

genrule(
    name = "renamed-deploy-jar",
    srcs = [
        ":integration_deploy.jar"
    ],
    outs = [
        "service.jar"
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
    tars = [
        ":bin_tar",
    ],
    workdir = "/usr/share/tink-backend-integration",
    tags = ["manual"],
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
