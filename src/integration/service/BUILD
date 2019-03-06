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
    name = "deploy-jar",
    srcs = [
        ":integration_deploy.jar"
    ],
    outs = [
        "integration_service.jar"
    ],
    cmd = "cp $(location :integration_deploy.jar) \"$(@)\"",
    visibility = ["//visibility:public"],
)

container_image(
    name = "integration_service",
    base = "@openjdk_jdk8//image",
    workdir = "/usr/share/integration_service",
    files = [
        ":integration_service.jar",
    ],
    directory = "/usr/share/integration_service"
)

container_push(
    name = "docker_push",
    format = "Docker",
    image = ":integration_service",
    registry = "gcr.io",
    repository = "tink-containers/tink-backend-integration",
    tag = "{TINK_VERSION}",
    tags = ["manual"],
)
