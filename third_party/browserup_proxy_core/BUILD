load("//tools/bzl:junit.bzl", "junit_test")
load("@com_github_johnynek_bazel_jar_jar//:jar_jar.bzl", "jar_jar")
load("@rules_java//java:defs.bzl", "java_binary", "java_import", "java_library")

# Fat jar with all third-party dependencies shaded
jar_jar(
    name = "browserup_proxy_core",
    input_jar = ":lib_deploy.jar",
    rules = "shade_rules.txt",
    visibility = ["//visibility:public"],
)

java_binary(
    name = "lib",
    main_class = "none",
    runtime_deps = ["@browserup_proxy_core//:com_browserup_browserup_proxy_core"],
)
