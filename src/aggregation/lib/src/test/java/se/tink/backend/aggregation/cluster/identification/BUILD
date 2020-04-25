load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "identification",
    srcs = glob(["*.java"]),
    runtime_deps = [
        "//third_party:com_sun_jersey_jersey_client",  # Prevents ClassNotFoundException: com.sun.ws.rs.ext.RuntimeDelegateImpl
    ],
    deps = [
        "//src/aggregation/api/src/main/java/se/tink/backend/aggregation/cluster/identification",
        "//src/libraries/uuid",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
