java_binary(
    name = "ukob_register",
    srcs = glob(["src/main/java/se/tink/backend/aggregation/UkobRegisterCommand.java"]),
    main_class = "se.tink.backend.aggregation.UkobRegisterCommand",
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents",
        "//src/integration/lib",
        "//src/libraries/serialization",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)
