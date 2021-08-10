load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "crypto",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/crypto/hash",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/encoding",
        "//third_party:commons_codec_commons_codec",
        "//third_party:net_minidev_json_smart",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "@aggregation//:com_google_guava_guava",
    ],
)
