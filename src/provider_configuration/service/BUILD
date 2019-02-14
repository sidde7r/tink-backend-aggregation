java_library(
    name = "service",
    srcs = glob(["src/main/**/*.java"]),
    data = [
        "//data",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/cli_print_utils",
        "//src/libraries/dropwizard_utils",
        "//src/libraries/field:rpc",
        "//src/libraries/provider:enums",
        "//src/libraries/repository",
        "//src/libraries/serialization",
        "//src/provider_configuration/lib",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_springframework_data_spring_data_jpa",
        "//third_party:org_springframework_spring_expression",
    ],
)

java_binary(
    name = "provider_configuration",
    srcs = glob(["src/main/**/*.java"]),
    data = [
        "//data",
        "//etc:development_minikube_provider_configuration_server",
        "//etc:development_provider_configuration_server",
    ],
    main_class = "se.tink.backend.aggregation.provider.ProviderConfigurationServiceContainer",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:mysql_mysql_connector_java",
    ],
    deps = [
        "//src/libraries/cli_print_utils",
        "//src/libraries/dropwizard_utils",
        "//src/libraries/field:rpc",
        "//src/libraries/provider:enums",
        "//src/libraries/repository",
        "//src/libraries/serialization",
        "//src/provider_configuration/lib",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_springframework_data_spring_data_jpa",
        "//third_party:org_springframework_spring_expression",
    ],
)

genrule(
    name = "renamed_provider_configuration_deploy",
    # Implicitly built, see: https://docs.bazel.build/versions/master/be/java.html#java_binary_implicit_outputs
    srcs = [":provider_configuration_deploy.jar"],
    outs = ["provider_configuration_service.jar"],
    cmd = "cp $(location :provider_configuration_deploy.jar) \"$(@)\"",
    visibility = ["//visibility:public"],
)
