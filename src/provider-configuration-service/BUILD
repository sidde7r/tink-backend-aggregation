java_library(
    name = "provider-configuration-service",
    srcs = glob(["src/main/**/*.java"]),
    data = [
        "//data",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/auth",
        "//src/libraries/cli_print_utils",
        "//src/libraries/discovery",
        "//src/libraries/dropwizard_utils:dropwizard-utils",
        "//src/libraries/metrics",
        "//src/libraries/provider:enums",
        "//src/libraries/repository",
        "//src/libraries/serialization",
        "//src/main-api",
        "//src/provider-configuration-api",
        "//src/provider-configuration-lib",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_springframework_data_spring_data_jpa",
        "//third_party:org_springframework_spring_expression",
    ],
)

java_binary(
    name = "provider-configuration",
    srcs = glob(["src/main/**/*.java"]),
    data = [
        "//data",
        "//etc:development-minikube-provider-configuration-server",
        "//etc:development-provider-configuration-server",
    ],
    main_class = "se.tink.backend.aggregation.provider.ProviderConfigurationServiceContainer",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:mysql_mysql_connector_java",
    ],
    deps = [
        "//src/libraries/auth",
        "//src/libraries/cli_print_utils",
        "//src/libraries/console_json_logger",
        "//src/libraries/discovery",
        "//src/libraries/dropwizard_utils:dropwizard-utils",
        "//src/libraries/metrics",
        "//src/libraries/provider:enums",
        "//src/libraries/repository",
        "//src/libraries/serialization",
        "//src/main-api",
        "//src/provider-configuration-api",
        "//src/provider-configuration-lib",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_springframework_data_spring_data_jpa",
        "//third_party:org_springframework_spring_expression",
    ],
)

genrule(
    name = "renamed-provider-configuration-deploy-jar",
    # Implicitly built, see: https://docs.bazel.build/versions/master/be/java.html#java_binary_implicit_outputs
    srcs = [":provider-configuration_deploy.jar"],
    outs = ["provider-configuration-service.jar"],
    cmd = "cp $(location :provider-configuration_deploy.jar) \"$(@)\"",
    visibility = ["//visibility:public"],
)
