def i18n_java_source(name, lang, visibility = None):
    """Generate the java source for a .po file.

    Generate the source to the local directory, then copy it to the place
    where Bazel expected it to reside. msgfmt is quite picky about how to generate
    these files so this is a bit more complicated that one would like."""

    cls = "se.tink.libraries.i18n_aggregation.Messages"
    path = cls.replace(".", "/")
    native.genrule(
        name = name,
        srcs = [lang + ".po"],
        outs = ["{path}_{lang}.java".format(lang = lang, path = path)],
        cmd = (
            "mkdir temp-{name};".format(name = name) +
            "msgfmt --source --java2 -d temp-{name}/ -r {cls} -l {lang} $(location {lang}.po)".format(
                name = name,
                lang = lang,
                cls = cls,
            ) +
            "&& cp temp-{name}/{path}*.java $@".format(name = name, path = path)
        ),
        visibility = visibility,
    )
