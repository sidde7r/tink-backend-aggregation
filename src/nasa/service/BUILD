load("@rules_pkg//:pkg.bzl", "pkg_tar")
load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_image",
    "container_push",
)
load("//tools/bzl:junit.bzl", "junit_test")

java_binary(
    name = "nasa",
    srcs = glob(["src/main/java/**/*.java"]),
    data = [
        "//data:security",
        "//etc/nasa:development-configuration",
    ],
    main_class = "se.tink.backend.nasa.boot.NASAService",
    resources = glob([
        "src/main/resources/**/*",
    ]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:log-runtime",
        "//third_party:netty-runtime",
    ],
    deps = [
        "//src/libraries/health_check_handler",
        "//src/libraries/serialization",
        "//src/libraries/simple_http_server",
        "//src/nasa/metrics",
        "//third_party:com_google_code_gson_gson",
        "//third_party:com_google_inject_guice",
        "//third_party:java_spark",
        "//third_party:log",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_yaml_snakeyaml",
        "//third_party:prometheus",
        "@aggregation//:com_google_guava_guava",
    ],
)

junit_test(
    name = "nasa_test",
    srcs = glob(["src/test/java/**/*.java"]),
    data = [
        "//data:security",
        "//etc/nasa:development-configuration",
    ],
    deps = [
        "//src/libraries/health_check_handler",
        "//src/libraries/simple_http_server",
        "//src/nasa/service:nasa",
        "//third_party:com_google_code_gson_gson",
        "//third_party:com_google_inject_guice",
        "//third_party:java_spark",
        "//third_party:log",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_yaml_snakeyaml",
        "//third_party:prometheus",
        "@aggregation//:com_google_guava_guava",
    ],
)

genrule(
    name = "renamed-deploy-jar",
    srcs = [
        ":nasa_deploy.jar",
    ],
    outs = [
        "service.jar",
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
    tags = ["manual"],
    tars = [
        ":bin_tar",
        ":data_tar",
    ],
    workdir = "/usr/share/tink-backend-notifying-aggregation-service-asserter",
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
