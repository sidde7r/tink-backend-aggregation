java_binary(
    name = "ukob-register",
    srcs = glob(["src/main/java/se/tink/backend/aggregation/UkobRegisterCommand.java"]),
    visibility = ["//visibility:public"],
    main_class = "se.tink.backend.aggregation.UkobRegisterCommand",
    deps = [
        "//src/agents-lib",
        "//src/libraries/serialization",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)
