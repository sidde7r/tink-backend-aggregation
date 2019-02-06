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

java_library(
    name = "i18n",
    srcs = [
        "//po:i18n_de",
        "//po:i18n_fr",
        "//po:i18n_nl",
        "//po:i18n_sv",
    ],
    visibility = ["//visibility:public"],
)
