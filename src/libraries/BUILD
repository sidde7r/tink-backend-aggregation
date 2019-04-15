load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "simple-http-server",
    groupId = "se.tink.backend.libraries.httpserver",
    visibility = ["//visibility:public"],
    exports = [
        "@tink_backend_shared_libraries//libraries/simple-http-server",
    ],
)

java_library_maven(
    name = "health-check-handler",
    groupId = "se.tink.backend.libraries.healthcheckhandler",
    visibility = ["//visibility:public"],
    exports = [
        "@tink_backend_shared_libraries//libraries/health-check-handler",
    ],
)
