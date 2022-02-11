load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "utils",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:commons_codec_commons_codec",
        "@aggregation//:org_apache_commons_commons_lang3",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_fasterxml_uuid_java_uuid_generator",
        "@tink_backend//src/libraries/uuid",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/exceptions",
        "//src/agent_sdk/sdk/src/models/field",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/agent_sdk/sdk/src/utils",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
