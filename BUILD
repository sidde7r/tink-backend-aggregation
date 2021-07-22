load("@rules_codeowners//tools:codeowners.bzl", "codeowners", "generate_codeowners")

package(default_visibility = ["//visibility:public"])

generate_codeowners(
    name = "generate_codeowners",
    owners = [
        ":aws_log_fetcher_owners",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/targobank:owners",
    ],
)

# If this test fails, copy the generated codeowners data to the CODEOWNERS file
sh_test(
    name = "validate_codeowners_up_to_date",
    srcs = ["@rules_codeowners//tools:diff.sh"],
    args = [
        "$(location :generate_codeowners.out)",
        "$(location CODEOWNERS)",
    ],
    data = [
        "CODEOWNERS",
        ":generate_codeowners.out",
    ],
)

codeowners(
    name = "aws_log_fetcher_owners",
    pattern = "/aws_log_fetcher/",
    teams = [
        "@tink-ab/aggregation-agent-platform-maintainer",
    ],
)

alias(
    name = "aggregation",
    actual = "//src/aggregation/service:bin",
)

alias(
    name = "convert_s3_to_aap",
    actual = "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/converter",
)

# Files needed by manual agent tests
filegroup(
    name = "agent_test_data",
    srcs = [
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
)

test_suite(
    name = "most_tests",
    tests = [
        "//src/aggregation/api:api_test",
        "//src/aggregation/lib/src/test/java/se/tink/backend/aggregation/wrappers",
        "//src/integration/lib:lib_test",
    ],
)

alias(
    name = "fmt",
    actual = "//tools/format:fmt",
    visibility = ["//:__pkg__"],
)

alias(
    name = "format",
    actual = "//tools/format:format-java-diff",
)

alias(
    name = "formatjson",
    actual = "//tools/format:format-json-diff",
)

# Workaround for annoying "no such target '//:.bazelproject'" error in recent plugin versions
exports_files([".bazelproject"])
