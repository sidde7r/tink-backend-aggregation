load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "document_container",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.documentcontainer",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:org_apache_hadoop_hadoop_core",
    ],
)
