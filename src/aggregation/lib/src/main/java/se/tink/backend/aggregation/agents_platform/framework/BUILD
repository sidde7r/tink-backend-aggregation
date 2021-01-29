load("//tools/bzl:junit.bzl", "junit_test")
load("@com_github_johnynek_bazel_jar_jar//:jar_jar.bzl", "jar_jar")
load("@rules_java//java:defs.bzl", "java_binary", "java_import", "java_library")

# Fat jar with all third-party dependencies shaded
jar_jar(
    name = "framework",
    input_jar = ":fat_jar_deploy.jar",
    rules = "shade_rules.txt",
    visibility = ["//visibility:public"],
)

java_binary(
    name = "fat_jar",
    main_class = "none",
    runtime_deps = ["@tink_backend_for_agents_framework//src/agents-platform/agents-platform-framework"],
)

java_binary(
    name = "fat_jar_sources",
    main_class = "none",
    runtime_deps = [":fat_jar_deploy-src.jar"],
)
