java_library(
    name = "versions",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/encrypted-payload",
        "//third_party:com_google_guava_guava",
        "//third_party:org_slf4j_slf4j_api",
        "@com_google_protobuf//:protobuf_java",
        "@tink_backend//src/credentials-revamp/common/models/proto:java_proto",
        "@tink_backend//src/libraries/serialization_utils",
    ],
)
