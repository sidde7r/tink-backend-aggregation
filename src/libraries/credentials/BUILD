load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "demo",
    srcs = glob(["src/main/**/demo/*.java"]),
    groupId = "se.tink.libraries.credentials",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_google_guava_guava",
    ],
)

junit_test(
    name = "demo_test",
    srcs = glob(["src/test/**/demo/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/credentials:demo",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)

junit_test(
    name = "service_test",
    srcs = glob(["src/test/**/service/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/credentials:service",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)

java_library_maven(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    groupId = "se.tink.libraries.credentials",
    visibility = ["//visibility:public"],
)

java_library_maven(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    groupId = "se.tink.libraries.credentials",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/credentials:enums",
        "//src/libraries/field:rpc",
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_google_guava_guava",
    ],
)

java_library_maven(
    name = "service",
    srcs = glob(["src/main/**/service/*.java"]),
    groupId = "se.tink.libraries.credentials",
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api",
        "//src/libraries/strings",
        "//src/libraries/user:rpc",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:commons_codec_commons_codec",
    ],
)
