java_library(
    name = "aggregation",
    data = [
        "//data:cryptography_test",
        "//data:provision_clients",
        "//data:tesseract_training_set",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
    ],
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//po:i18n",
        # Agent runtime deps have been moved to //se/tink/backend/aggregation/agents/agentfactory
    ],
)
