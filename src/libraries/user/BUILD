java_library(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
    ],
)

java_library(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/date",
        "//src/libraries/enums",
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "//src/libraries/user:enums",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_google_guava_guava",
        "//third_party:io_protostuff_protostuff_api",
        "//third_party:io_swagger_swagger_annotations",
        "//third_party:org_hibernate_hibernate_annotations",
        "//third_party:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
        "@com_google_code_findbugs_jsr305//jar",
    ],
)
