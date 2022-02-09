load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:gettext.bzl", "i18n_java_source")

i18n_java_source(
    name = "i18n_sv",
    lang = "sv",
)

i18n_java_source(
    name = "i18n_fr",
    lang = "fr",
)

i18n_java_source(
    name = "i18n_nl",
    lang = "nl",
)

i18n_java_source(
    name = "i18n_de",
    lang = "de",
)

i18n_java_source(
    name = "i18n_pt",
    lang = "pt",
)

i18n_java_source(
    name = "i18n_it",
    lang = "it",
)

i18n_java_source(
    name = "i18n_lt",
    lang = "lt",
)

i18n_java_source(
    name = "i18n_lv",
    lang = "lv",
)

i18n_java_source(
    name = "i18n_no",
    lang = "no",
)

i18n_java_source(
    name = "i18n_da",
    lang = "da",
)

i18n_java_source(
    name = "i18n_fi",
    lang = "fi",
)

i18n_java_source(
    name = "i18n_es",
    lang = "es",
)

i18n_java_source(
    name = "i18n_et",
    lang = "et",
)

i18n_java_source(
    name = "i18n_pl",
    lang = "pl",
)

java_library(
    name = "i18n_aggregation",
    srcs = [
        "//po:i18n_da",
        "//po:i18n_de",
        "//po:i18n_es",
        "//po:i18n_et",
        "//po:i18n_fi",
        "//po:i18n_fr",
        "//po:i18n_it",
        "//po:i18n_lt",
        "//po:i18n_lv",
        "//po:i18n_nl",
        "//po:i18n_no",
        "//po:i18n_pl",
        "//po:i18n_pt",
        "//po:i18n_sv",
    ],
    visibility = ["//visibility:public"],
)

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-fluffy-maintainer",
        "@tink-ab/aggregation-thundercats-maintainer",
        "@tink-ab/aggregation-minion-maintainer",
        "@tink-ab/aggregation-nazguls-maintainer",
        "@tink-ab/aggregation-teletubbies-maintainer",
        "@tink-ab/aggregation-llamas-maintainer",
        "@tink-ab/aggregation-penguins-maintainer",
        "@tink-ab/aggregation-toros-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
