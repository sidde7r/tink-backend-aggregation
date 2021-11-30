load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("@rules_java//java:defs.bzl", "java_plugin")

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/agent-effectiveness-maintainer",
    ],
    visibility = ["//:__pkg__"],
)

java_plugin(
    name = "lombok_plugin",
    generates_api = 1,
    processor_class = "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
    visibility = [
        "//src/agent_sdk:__subpackages__",
    ],
    deps = [
        "@agent_sdk_maven//:org_projectlombok_lombok",
    ],
)
