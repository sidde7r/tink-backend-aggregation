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
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_netflix_governator",
        "//third_party:io_dropwizard_dropwizard_core",
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
        "//src/aggregation/service",
        "//third_party:mysql_mysql_connector_java",
    ],
)

genrule(
    name = "bin_deploy",
    # Implicitly built, see: https://docs.bazel.build/versions/master/be/java.html#java_binary_implicit_outputs
    srcs = [":bin_deploy.jar"],
    outs = ["aggregation_service.jar"],
    cmd = "cp $(location :bin_deploy.jar) \"$(@)\"",
    visibility = ["//visibility:public"],
)
