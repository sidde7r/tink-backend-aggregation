java_library(
    name = "event",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:javax_inject_javax_inject",
        "@tink_backend//src/libraries/event_producer_service_client",
        "@tink_backend//src/libraries/events:event_submitter_api",
        "@tink_backend//src/libraries/events:event_submitter_batching",
        "@tink_backend//src/libraries/events:event_submitter_configuration",
        "@tink_backend//src/libraries/events:event_submitter_mock",
        "@tink_backend//src/libraries/events:event_submitter_module",
    ],
)
