load("@bazel_common//tools/maven:pom_file.bzl", "pom_file")
load("@bazel_tools//tools/build_defs/pkg:pkg.bzl", "pkg_tar")

def java_library_maven(name, groupId, artifactId = None, version = None, **kwargs):
    groupId = groupId.replace("_", "").replace("-","")
    artifactId = (artifactId or name).replace("_", "-")
    version = version or "1.0.0-SNAPSHOT"
    native.java_library(name = name, **kwargs)
    # generate Maven POM, required by @rules_jvm_external
    pom_file(
        name = "%s_pom" % name,
        substitutions = {
            "GROUP_ID": groupId,
            "ARTIFACT_ID": artifactId,
            "VERSION": version,
        },
        targets = [
            ":%s" % name,
        ],
        template_file = "//tools/bzl:pom_template",
    )
    native.genrule(
        name = "%s_renamed" % name,
        srcs = [
            "lib%s.jar" % name,
        ],
        outs = [
            "%s-%s.jar" % (artifactId, version),
        ],
        tags = ["maven_coordinates=%s:%s:%s" % (groupId, artifactId, version)],
        cmd = "cp $< $@",
    )
    native.genrule(
        name = "%s_pom_renamed" % name,
        srcs = [
            "%s_pom.xml" % name,
        ],
        outs = [
            "%s-%s.pom" % (artifactId, version),
        ],
        tags = ["maven_coordinates=%s:%s:%s" % (groupId, artifactId, version)],
        cmd = "cp $< $@",
    )
    pkg_tar(
        name = "%s_tar" % name,
        extension = "tar.gz",
        # creates the structure within the tar, se/tink/libraries/account/rpc/1.0.0-SNAPSHOT/...
        package_dir = groupId.replace(".", "/") + "/" + artifactId + "/" + version,
        srcs = [
            "%s_renamed" % name,
            "%s_pom_renamed" % name,
        ],
    )
    native.genrule(
        name = "%s_mvn" % name,
        srcs = [
            "%s_tar" % name
        ],
        outs = [
            "%s-%s.tar.gz" % (artifactId, version),
        ],
        tags = ["maven_coordinates=%s:%s:%s" % (groupId, artifactId, version)],
        cmd = "cp $< $@",
    )
