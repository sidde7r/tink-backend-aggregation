java_library(
    name = "grpc",
    licenses = ["notice"],
    visibility = ["//visibility:public"],
    exports = [
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@io_grpc_grpc_api//jar",
        "@io_grpc_grpc_core//jar",
        "@io_grpc_grpc_services//jar",
    ],
    runtime_deps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_google_instrumentation_instrumentation_api",
        "@aggregation//:io_opencensus_opencensus_api",
        "@aggregation//:io_perfmark_perfmark_api",
        "@aggregation_temp//:io_opencensus_opencensus_contrib_grpc_metrics",
    ],
)

java_library(
    name = "grpc-api",
    licenses = ["notice"],
    visibility = ["//visibility:public"],
    exports = [
        "@io_grpc_grpc_api//jar",
    ],
    runtime_deps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:io_opencensus_opencensus_api",
        "@aggregation//:io_perfmark_perfmark_api",
        "@aggregation_temp//:io_opencensus_opencensus_contrib_grpc_metrics",
    ],
)
