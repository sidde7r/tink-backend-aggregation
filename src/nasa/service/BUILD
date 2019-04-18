load("//tools/bzl:junit.bzl", "junit_test")
load("@bazel_tools//tools/build_defs/pkg:pkg.bzl", "pkg_tar")
load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_image",
    "container_push",
)

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
        "@maven//:io_netty_netty_tcnative_boringssl_static",
    ],
    deps = [
        "//src/libraries/serialization",
        "//src/nasa/metrics",
        "@maven//:com_sparkjava_spark_core",
        "//third_party:log",
        "@maven//:com_google_code_gson_gson",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:io_prometheus_simpleclient",
        "@maven//:io_prometheus_simpleclient_hotspot",
        "@maven//:io_prometheus_simpleclient_servlet",
        "@maven//:org_apache_httpcomponents_httpclient",
        "@maven//:org_apache_httpcomponents_httpcore",
        "@maven//:org_yaml_snakeyaml",
        "@tink_backend_shared_libraries//libraries/health-check-handler",
        "@tink_backend_shared_libraries//libraries/simple-http-server",
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
        ":nasa",
        "//src/libraries/serialization",
        "//src/nasa/metrics",
        "//third_party:log",
        "@maven//:com_google_code_gson_gson",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:com_sparkjava_spark_core",
        "@maven//:org_apache_httpcomponents_httpclient",
        "@maven//:org_apache_httpcomponents_httpcore",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_yaml_snakeyaml",
        "@tink_backend_shared_libraries//libraries/health-check-handler",
        "@tink_backend_shared_libraries//libraries/simple-http-server",
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
