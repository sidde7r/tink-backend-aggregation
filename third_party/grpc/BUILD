java_library(
    name = "grpc",
    licenses = ["notice"],
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@grpc_libraries//:io_grpc_grpc_api",
        "@grpc_libraries//:io_grpc_grpc_core",
        "@grpc_libraries//:io_grpc_grpc_services",
    ],
    runtime_deps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_google_instrumentation_instrumentation_api",
        "@aggregation//:io_opencensus_opencensus_api",
        "@aggregation//:io_opencensus_opencensus_contrib_grpc_metrics",
        "@aggregation//:io_perfmark_perfmark_api",
    ],
)

java_library(
    name = "grpc-api",
    licenses = ["notice"],
    visibility = ["//visibility:public"],
    exports = [
        "@grpc_libraries//:io_grpc_grpc_api",
    ],
    runtime_deps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:io_opencensus_opencensus_api",
        "@aggregation//:io_opencensus_opencensus_contrib_grpc_metrics",
        "@aggregation//:io_perfmark_perfmark_api",
    ],
)
