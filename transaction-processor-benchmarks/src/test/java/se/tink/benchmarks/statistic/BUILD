java_test(
    name = "benchmarks",
    test_class = "se.tink.benchmarks.statistic.StatisticGenerationBenchmarks",
    srcs = glob(["*.java"]),
    tags = [
        "external",
        "manual",
    ],
    runtime_deps = [
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        "//:common-lib",
        "//:main-api",
        "//src/categorization-api:categorization-api",
        "//src/libraries/date:date",
        "//src/libraries/metrics:metrics",
        "//src/libraries/uuid:uuid",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_mockito_mockito_core",
    ]
)
