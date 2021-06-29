load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "lifecycle",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        # Intentionally drag in transitive dependency closure of aggregation service to classpath
        "//src/aggregation/service",
    ],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/configuration",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/daos",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/worker",
        "//src/aggregation/service",
        "//src/integration/agent_data_availability_tracker/client",
        "//src/integration/tpp_secrets_service/client/src/main/java/se/tink/backend/integration/tpp_secrets_service/client:tpp-secrets-service-client",
        "//src/libraries/draining",
        "//src/libraries/queue_lib",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_configuration",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_jetty",
        "//third_party:io_dropwizard_dropwizard_logging",
        "//third_party:io_dropwizard_dropwizard_metrics",
        "//third_party:io_prometheus_simpleclient",
        "//third_party:javax_validation_validation_api",
        "//third_party:jetty_server9",
        "//third_party:org_mockito_mockito_core",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/prometheus",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/registry",
        "@tink_backend//src/libraries/unleash",
    ],
)
