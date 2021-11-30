load("@rules_python//python:defs.bzl", "py_binary", "py_test")

py_binary(
    name = "compile_srcjar",
    srcs = ["compile_srcjar.py"],
    srcs_version = "PY3",
    visibility = ["//visibility:public"],
)

py_test(
    name = "test_compile_srcjar",
    srcs = ["tests/test_compile_srcjar.py"],
    imports = ["."],
    srcs_version = "PY3",
    deps = ["compile_srcjar"],
)
