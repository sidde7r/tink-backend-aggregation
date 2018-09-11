load("//tools/bzl:gettext.bzl", "i18n_java_source")

i18n_java_source(
    name = "i18n_en_US",
    lang = "en_US",
)

i18n_java_source(
    name = "i18n_sv",
    lang = "sv",
)

i18n_java_source(
    name = "i18n_en_GB",
    lang = "en_GB",
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
        ":i18n_en_US",
        ":i18n_en_GB",
        ":i18n_fr",
        ":i18n_de",
        ":i18n_nl",
        ":i18n_sv",
    ],
    visibility = ["//visibility:public"],
)
