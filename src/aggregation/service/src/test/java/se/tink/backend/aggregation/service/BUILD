load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "service",
    srcs = glob(["*.java"]),
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
    ],
    deps = [
        "@system_tests//:org_hamcrest_hamcrest_core",
        "@system_tests//:org_hamcrest_hamcrest_library",
        "@system_tests//:org_springframework_boot_spring_boot_test",
        "@system_tests//:org_springframework_spring_test",
        "@system_tests//:org_springframework_spring_web",
    ],
)
