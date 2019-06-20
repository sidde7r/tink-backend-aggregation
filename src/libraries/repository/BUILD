load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "repository",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.repository",
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:c3p0_c3p0",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:javax_validation_validation_api",
        "@maven//:org_hibernate_hibernate_validator",
        "@maven//:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:org_springframework_data_spring_data_commons",
        "@maven//:org_springframework_data_spring_data_jpa",
        "@maven//:org_springframework_spring_context",
        "@maven//:org_springframework_spring_core",
        "@maven//:org_springframework_spring_expression",
        "@maven//:org_springframework_spring_orm",
        "@maven//:org_springframework_spring_tx",
    ],
)
