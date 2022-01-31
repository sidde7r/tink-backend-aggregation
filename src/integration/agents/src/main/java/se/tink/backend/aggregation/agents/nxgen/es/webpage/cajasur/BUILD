load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "cajasur",
    srcs = glob(["**/*.java"]),
    data = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/es/webpage/cajasur/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/common",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/libraries/agent-storage",
        "//src/libraries/serialization",
        "//third_party:com_google_inject_guice",
        "//third_party:commons_io_commons_io",
        "//third_party:lombok_libs",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_jsoup_jsoup",
        "//third_party:org_mozilla_rhino",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_springframework_spring_web",
        "@tink_backend//src/libraries/connectivity_errors/proto:java_proto",
    ],
)
