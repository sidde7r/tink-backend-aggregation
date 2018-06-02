load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "categorization-lib",
    srcs = glob(["src/main/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
        "//:main-api",
        "//src/categorization-api",
        "//:common-lib",
        "//:common-utilities",

        "//src/libraries/log:log",
        "//src/libraries/abnamro:abn_amro",
        "//src/libraries/cluster:cluster",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/metrics:metrics",
        "//src/libraries/http_client:http-client",
        "//src/libraries/jersey_utils:jersey-utils",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:org_apache_hadoop_hadoop_core",
        "//third_party:org_apache_mahout_mahout_core",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:org_elasticsearch_elasticsearch",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_code_findbugs_jsr305",
        ":fasttext-test-model",

        "@org_apache_curator_curator_framework//jar",
    ],
    visibility = ["//visibility:public"],
)

genrule(
  name = "fasttext-test-model",
  srcs = ["//data:fasttext-training-set"],
  outs = ["minimal_model.bin"],
  tools = ["@fasttext//:fasttext"],
  cmd = "$(location @fasttext//:fasttext) supervised -input $< -output $(@D)/`basename $@ .bin`",
  output_to_bindir = 0,
  visibility = ["//visibility:public"],
)

junit_test(
    name = "categorization-lib-test",
    srcs = glob(["src/test/**/*.java"]),
    resources = [
        "@fasttext//:fasttext",
        "minimal_model.bin",
    ],
    runtime_deps = [
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        "//src/libraries/abnamro:abn_amro",
        "//src/libraries/cluster:cluster",
        "//src/categorization-api",
        "//src/categorization-lib",
        "//:common-lib",
        "//:main-api",
        "//:main-api-testlib",
        "//third_party:org_assertj_assertj_core",
        "//third_party:com_google_guava_guava",
        "//third_party:org_mockito_mockito_core",
        "//third_party:com_google_code_findbugs_jsr305",
    ],
)
