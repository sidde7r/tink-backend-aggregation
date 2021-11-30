load("@rules_java//java:defs.bzl", "java_library")

def to_label(target):
    if target.startswith("//"):
        return Label(target)
    return Label("//" + native.package_name()).relative(target)

def java_library_with_sources(**attrs):
    if "deps" in attrs:
        fail("java_library_with_sources does not accept deps, please use 'intdeps' and 'extdeps'")
    if "intdeps" not in attrs and "srcs" not in attrs:
        fail("java_library_with_sources must have at least one of either 'srcs' or 'intdeps'")

    intdeps = attrs.pop("intdeps", [])
    srcjardeps = []
    extdeps = attrs.pop("extdeps", [])
    exports = attrs.get("exports", [])

    newdeps = []
    for dep in intdeps:
        newdeps.append(dep)
        l = to_label(dep)
        srcjardeps.append(l.relative(l.name + "_srcjar"))
    for dep in extdeps:
        newdeps.append(dep)

    if len(newdeps) > 0 and "srcs" in attrs:
        attrs["deps"] = newdeps

    java_library(**attrs)
    allsrcs = []
    if "srcs" in attrs:
        allsrcs += attrs["srcs"]
    allsrcs += srcjardeps

    native.genrule(
        name = attrs["name"] + "_srcjar",
        outs = [attrs["name"] + ".srcjar"],
        cmd = "$(location //tools/bzl/compile_srcjar) $@ $(SRCS)",
        tools = ["//tools/bzl/compile_srcjar"],
        visibility = attrs.get("visibility", None),
        srcs = allsrcs,
    )
