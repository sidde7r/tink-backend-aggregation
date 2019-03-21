load("@bazel_tools//tools/build_defs/pkg:pkg.bzl", "pkg_tar")
load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_image",
    "container_push",
)

java_binary(
    name = "nasa",
    srcs = glob(["src/main/java/**/*.java"]),
    main_class = "se.tink.backend.nasa.boot.NASAService",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:log-runtime",
        "//third_party:netty-runtime",
    ],
    resources = glob([
        "src/main/resources/**/*",
    ]),
    data = [
        "//etc/nasa:development-configuration",
        "//data:security",
    ],
    deps = [
        "//src/libraries:simple-http-server",
        "//src/libraries:health-check-handler",

        "//third_party:log",
        "//third_party:prometheus",
        "//third_party:com_google_inject_guice",
        "//third_party:java_spark",

        "//src/nasa/metrics:metrics",

        "@org_apache_logging_log4j_log4j_slf4j//jar",

        "//third_party:com_google_guava_guava",
        "//third_party:org_yaml_snakeyaml",
    ],
)

genrule(
    name = "renamed-deploy-jar",
    srcs = [
        ":nasa_deploy.jar"
    ],
    outs = [
        "service.jar"
    ],
    cmd = "cp $(location :nasa_deploy.jar) \"$(@)\"",
    visibility = ["//visibility:public"],
)

pkg_tar(
    name = "bin_tar",
    srcs = [":renamed-deploy-jar"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-notifying-aggregation-service-asserter",
    tags = ["manual"],
)

pkg_tar(
    name = "data_tar",
    srcs = ["//data:security"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-notifying-aggregation-service-asserter/data",
    strip_prefix = "/data",
    tags = ["manual"],
)

container_image(
    name = "docker",
    base = "@openjdk_jdk8//image",
    tars = [
        ":bin_tar",
        ":data_tar",
    ],
    workdir = "/usr/share/tink-backend-notifying-aggregation-service-asserter",
    tags = ["manual"],
)

container_push(
    name = "docker_push",
    format = "Docker",
    image = ":docker",
    registry = "gcr.io",
    repository = "tink-containers/tink-backend-notifying-aggregation-service-asserter",
    tag = "{TINK_VERSION}",
    tags = ["manual"],
)
