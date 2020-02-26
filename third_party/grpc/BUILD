java_library(
    name = "grpc",
    licenses = ["notice"],
    visibility = ["//visibility:public"],
    exports = [
        "@com_google_code_findbugs_jsr305//jar",
        "@io_grpc_grpc_api//jar",
        "@io_grpc_grpc_core//jar",
        "@io_grpc_grpc_services//jar",
    ],
    runtime_deps = [
        "//third_party:com_google_instrumentation_instrumentation_api",
        "@aggregation//:com_google_guava_guava",
        "@io_opencensus_opencensus_api//jar",
        "@io_opencensus_opencensus_contrib_grpc_metrics//jar",
        "@io_perfmark_perfmark_api//jar",
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
        "@io_opencensus_opencensus_api//jar",
        "@io_opencensus_opencensus_contrib_grpc_metrics//jar",
        "@io_perfmark_perfmark_api//jar",
    ],
)
