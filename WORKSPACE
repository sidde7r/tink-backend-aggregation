# Use the new Skylark version of git_repository. This uses the system's native
# git client which supports fancy key formats and key passphrases.
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")

http_archive(
    name = "bazel_skylib",
    sha256 = "97e70364e9249702246c0e9444bccdc4b847bed1eb03c5a3ece4f83dfe6abc44",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/1.0.2/bazel-skylib-1.0.2.tar.gz",
        "https://github.com/bazelbuild/bazel-skylib/releases/download/1.0.2/bazel-skylib-1.0.2.tar.gz",
    ],
)

load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")

bazel_skylib_workspace()

# This checks that the version of Bazel in use is at least the set version
# Usually this should be set to the version of Bazel used for CI
load("@bazel_skylib//lib:versions.bzl", "versions")

versions.check("0.28.1", "0.29.1")

# rules_pkg
http_archive(
    name = "rules_pkg",
    sha256 = "4ba8f4ab0ff85f2484287ab06c0d871dcb31cc54d439457d28fd4ae14b18450a",
    url = "https://github.com/bazelbuild/rules_pkg/releases/download/0.2.4/rules_pkg-0.2.4.tar.gz",
)

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")

rules_pkg_dependencies()

http_archive(
    name = "rules_java",
    sha256 = "220b87d8cfabd22d1c6d8e3cdb4249abd4c93dcc152e0667db061fb1b957ee68",
    url = "https://github.com/bazelbuild/rules_java/releases/download/0.1.1/rules_java-0.1.1.tar.gz",
)

load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")

rules_java_dependencies()

rules_java_toolchains()

## Tink virtual monorepsotiroy
# These are repositories under Tink control. They are trusted, and imported
# as a part of tink-backend's repository (not checksumed).

git_repository(
    name = "dropwizard_jersey",
    commit = "0c2f90f4358e262d0fe0af3f6d31eb0fa3cabc40",
    remote = "git@github.com:tink-ab/dropwizard.git",
    shallow_since = "1490898663 +0200",
)

git_repository(
    name = "tink_httpcore_4_4_9",
    commit = "0f72fa2c392fee8388d327cb3462cd10d675c2e2",
    remote = "git@github.com:tink-ab/httpcomponents-core.git",
    shallow_since = "1537528950 +0200",
)

git_repository(
    name = "tink_httpclient_4_5_5",
    commit = "1ed65fa09a4b7bc9f469fbb3625ac5b087f9cc3e",
    remote = "git@github.com:tink-ab/httpcomponents-client.git",
    shallow_since = "1537529121 +0200",
)

git_repository(
    name = "com_tink_api_grpc",
    commit = "5d60e5719749287d076bf8c84729bdd86d119823",
    remote = "git@github.com:tink-ab/tink-grpc.git",
    shallow_since = "1575523605 +0000",
)

git_repository(
    name = "tink_backend",
    commit = "92d2e6c7adca04e074ab3351ba8a9404244f6765",
    remote = "git@github.com:tink-ab/tink-backend.git",
    shallow_since = "1572535216 +0000",
)

# Docker dependencies
http_archive(
    name = "io_bazel_rules_docker",
    sha256 = "aed1c249d4ec8f703edddf35cbe9dfaca0b5f5ea6e4cd9e83e99f3b0d1136c3d",
    strip_prefix = "rules_docker-0.7.0",
    urls = ["https://github.com/bazelbuild/rules_docker/archive/v0.7.0.tar.gz"],
)

# Google api types.
http_archive(
    name = "com_google_googleapis",
    sha256 = "ddd2cd7b6b310028b8ba08057d2990ced6f78c35fdf5083ff142704f1c2c5e49",
    strip_prefix = "googleapis-10049e8ea946100bb7da66f63b0ecd1a345e8760",
    urls = ["https://github.com/googleapis/googleapis/archive/10049e8ea946100bb7da66f63b0ecd1a345e8760.zip"],
)

load("@com_google_googleapis//:repository_rules.bzl", "switched_rules_by_language")

switched_rules_by_language(
    name = "com_google_googleapis_imports",
    grpc = True,
    java = True,
)

# This is NOT needed when going through the language lang_image
# "repositories" function(s).
load(
    "@io_bazel_rules_docker//repositories:repositories.bzl",
    container_repositories = "repositories",
)

container_repositories()

load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_pull",
)

container_pull(
    name = "openjdk_jdk8",
    registry = "gcr.io",
    repository = "tink-containers/openjdk-8-jre",
    tag = "8",
)

## External dependencies
# External repositories that are not under Tink control. These should *always*
# be locked to something stable (a commit, not a branch for example) and have
# a checksum to prevent tampering by the remote end.

# libm4ri library, needed by https://github.com/tink-ab/tink-backend-aggregation/tree/master/tools/libkbc_wbaes_src
http_file(
    name = "libm4ri_dev",
    downloaded_file_path = "libm4ri-dev_20140914-2+b1_amd64.deb",
    sha256 = "040b81df10945380424d8874d38c062f45a5fee6886ae8e6963c87393ba84cd9",
    urls = ["http://ftp.br.debian.org/debian/pool/main/libm/libm4ri/libm4ri-dev_20140914-2+b1_amd64.deb"],
)

http_file(
    name = "libm4ri_0.0.20140914",
    downloaded_file_path = "libm4ri-0.0.20140914_20140914-2+b1_amd64.deb",
    sha256 = "c2f38d51730b6e9a73e2f4d2e0edfadf647a9889da9d06a15abca07d3eccc6f1",
    urls = ["http://ftp.br.debian.org/debian/pool/main/libm/libm4ri/libm4ri-0.0.20140914_20140914-2+b1_amd64.deb"],
)

# proto_library rules implicitly depend on @com_google_protobuf//:protoc,
# which is the proto-compiler.
PROTOBUF_VERSION = "3.9.0"

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
GRPC_JAVA_VERSION = "1.23.0"

GRPC_JAVA_NANO_VERSION = "1.21.1"

http_file(
    name = "protoc_gen_grpc_java_linux_x86_64",
    sha256 = "b3823d7bca0c3513d48ef43de63f6a48410040f5f7b16d5eceea0adb98d07f42",
    urls = ["http://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/%s/protoc-gen-grpc-java-%s-linux-x86_64.exe" % (GRPC_JAVA_VERSION, GRPC_JAVA_VERSION)],
)

http_file(
    name = "protoc_gen_grpc_java_macosx",
    sha256 = "e5e514c76264f3cd8f26c19628a6fc5db2c355b1d285d252aa9b91c136b0f025",
    urls = ["http://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/%s/protoc-gen-grpc-java-%s-osx-x86_64.exe" % (GRPC_JAVA_VERSION, GRPC_JAVA_VERSION)],
)

http_file(
    name = "protoc_gen_grpc_java_windows_x86_64",
    sha256 = "8e8b2b3a0b5b083cf5fc0268da7dd6b305762e591ec4a468e5e688f77c32e63f",
    urls = ["https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/%s/protoc-gen-grpc-java-%s-windows-x86_64.exe" % (GRPC_JAVA_VERSION, GRPC_JAVA_VERSION)],
)

# rules_proto will not be builtin in to Bazel in v1.0 and later
# prepare us for that, and use the out-ot-repo version
http_archive(
    name = "rules_proto",
    sha256 = "57001a3b33ec690a175cdf0698243431ef27233017b9bed23f96d44b9c98242f",
    strip_prefix = "rules_proto-9cd4f8f1ede19d81c6d48910429fe96776e567b1",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_proto/archive/9cd4f8f1ede19d81c6d48910429fe96776e567b1.tar.gz",
        "https://github.com/bazelbuild/rules_proto/archive/9cd4f8f1ede19d81c6d48910429fe96776e567b1.tar.gz",
    ],
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

http_archive(
    name = "com_google_protobuf",
    sha256 = "8eb5ca331ab8ca0da2baea7fc0607d86c46c80845deca57109a5d637ccb93bb4",
    strip_prefix = "protobuf-%s" % PROTOBUF_VERSION,
    urls = ["https://github.com/protocolbuffers/protobuf/archive/v%s.zip" % PROTOBUF_VERSION],
)

bind(
    name = "protocol_compiler",
    actual = "@com_google_protobuf//:protoc",
)

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

OPENCENSUS_VERSION = "0.21.0"

maven_jar(
    name = "io_opencensus_opencensus_api",
    artifact = "io.opencensus:opencensus-api:%s" % OPENCENSUS_VERSION,
    sha1 = "73c07fe6458840443f670b21c7bf57657093b4e1",
)

maven_jar(
    name = "io_opencensus_opencensus_contrib_grpc_metrics",
    artifact = "io.opencensus:opencensus-contrib-grpc-metrics:%s" % OPENCENSUS_VERSION,
    sha1 = "f07d3a325f1fe69ee40d6b409086964edfef4e69",
)

maven_jar(
    name = "io_perfmark_perfmark_api",
    artifact = "io.perfmark:perfmark-api:0.17.0",
    sha1 = "97e81005e3a7f537366ffdf20e11e050303b58c1",
)

maven_jar(
    name = "com_google_errorprone_error_prone_annotations",
    artifact = "com.google.errorprone:error_prone_annotations:2.0.11",
    sha1 = "3624d81fca4e93c67f43bafc222b06e1b1e3b260",
)

## Maven jar imports
# Same as above, always make sure to specify a checksum. To check what is using
# a specifc library you can use:
# $ bazel query 'rdeps(//:all, @aopalliance_aopalliance//jar, 1)'
# //third_party:org_springframework_data_spring_cql
# //third_party:com_google_inject_guice
# @aopalliance_aopalliance//jar:jar

maven_jar(
    name = "aopalliance_aopalliance",
    artifact = "aopalliance:aopalliance:1.0",
    sha1 = "0235ba8b489512805ac13a8f9ea77a1ca5ebe3e8",
)

maven_jar(
    name = "org_apache_lucene_lucene_grouping",
    artifact = "org.apache.lucene:lucene-grouping:4.4.0",
    sha1 = "c0ead8ee2937ed00a60efe825e3b6ae1e1f268d9",
)

maven_jar(
    name = "org_eclipse_jetty_toolchain_setuid_jetty_setuid_java",
    artifact = "org.eclipse.jetty.toolchain.setuid:jetty-setuid-java:1.0.2",
    sha1 = "4dc7fca46ac6badff4336c574b2c713ac3e40f73",
)

maven_jar(
    name = "io_dropwizard_dropwizard_core",
    artifact = "io.dropwizard:dropwizard-core:0.7.1",
    sha1 = "569cd6181d5fea4b033cf3a0eca632c8931a1a3a",
)

maven_jar(
    name = "org_mozilla_rhino",
    artifact = "org.mozilla:rhino:1.7R4",
    sha1 = "e982f2136574b9a423186fbaeaaa98dc3e5a5288",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_server",
    artifact = "org.eclipse.jetty:jetty-server:9.0.7.v20131107",
    sha1 = "682ae23f9e4a5e397d96f215b62641755d2a59b7",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_proxy",
    artifact = "org.eclipse.jetty:jetty-proxy:9.0.7.v20131107",
    sha1 = "5d16b84cf4ff40ef743c72ec3ffb118553f709c1",
)

maven_jar(
    name = "com_codahale_metrics_metrics_httpclient",
    artifact = "com.codahale.metrics:metrics-httpclient:3.0.2",
    sha1 = "c658daf41b1ecf934ccd21e83eeeb18703355afb",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_resolver",
    artifact = "io.netty:netty-resolver:4.1.38.Final",
    sha1 = "b00be4aa309e9b56e498191aa8c73e4f393759ed",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_resolver_dns",
    artifact = "io.netty:netty-resolver-dns:4.1.38.Final",
    sha1 = "a628b322a1a7fadc427edc15eb3c141d50706437",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_codec_dns",
    artifact = "io.netty:netty-codec-dns:4.1.38.Final",
    sha1 = "cfc06c8566e4bf060a0360e28865e70e37d150e8",
)

maven_jar(
    name = "net_sourceforge_argparse4j_argparse4j",
    artifact = "net.sourceforge.argparse4j:argparse4j:0.4.3",
    sha1 = "f4c256934d79940477a35b6c4c182b47ee5f0a6f",
)

maven_jar(
    name = "com_google_inject_guice",
    artifact = "com.google.inject:guice:4.1.0",
    sha1 = "eeb69005da379a10071aa4948c48d89250febb07",
)

maven_jar(
    name = "com_google_inject_extensions_guice_grapher",
    artifact = "com.google.inject.extensions:guice-grapher:4.1.0",
    sha1 = "5b5e42aef1d8990bed1617eeaf1f3854dec677e2",
)

maven_jar(
    name = "com_google_inject_extensions_guice_assistedinject",
    artifact = "com.google.inject.extensions:guice-assistedinject:4.1.0",
    sha1 = "af799dd7e23e6fe8c988da12314582072b07edcb",
)

maven_jar(
    name = "com_google_googlejavaformat",
    artifact = "com.google.googlejavaformat:google-java-format:1.7",
    sha1 = "97cb6afc835d65682edc248e19170a8e4ecfe4c4",
)

maven_jar(
    name = "com_google_errorprone",
    artifact = "com.google.errorprone:javac-shaded:9+181-r4173-1",
    sha1 = "a399ee380b6d6b6ea53af1cfbcb086b108d1efb7",
)

maven_jar(
    name = "com_netflix_governator",
    artifact = "com.netflix.governator:governator:1.17.2",
    sha1 = "0738b9a37389339f86ad68f4d74fc59fa69e0fb6",
)

maven_jar(
    name = "com_netflix_governator_api",
    artifact = "com.netflix.governator:governator-api:1.17.2",
    sha1 = "72eb81c0449dc7ca5ed24fb200e16b3228f6dd91",
)

maven_jar(
    name = "com_netflix_governator_core",
    artifact = "com.netflix.governator:governator-core:1.17.2",
    sha1 = "c9e0129f8526b8f0b44ffcae0d74feb6b7b71b55",
)

maven_jar(
    name = "com_googlecode_libphonenumber_libphonenumber",
    artifact = "com.googlecode.libphonenumber:libphonenumber:5.7",
    sha1 = "20140c130456845cc73f3b2a4bf50c7fe3a37b77",
)

maven_jar(
    name = "software_amazon_ion_ion_java",
    artifact = "software.amazon.ion:ion-java:1.0.2",
    sha1 = "ee9dacea7726e495f8352b81c12c23834ffbc564",
)

maven_jar(
    name = "com_fasterxml_jackson_dataformat_jackson_dataformat_cbor",
    artifact = "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.9.9",
    sha1 = "3206f36ea2b0f9bd365a138338281243241dc9da",
)

maven_jar(
    name = "com_codahale_metrics_metrics_logback",
    artifact = "com.codahale.metrics:metrics-logback:3.0.2",
    sha1 = "3bfec071f1ec390d10b17959271dc54195d2ab0d",
)

maven_jar(
    name = "javax_validation_validation_api",
    artifact = "javax.validation:validation-api:1.1.0.Final",
    sha1 = "8613ae82954779d518631e05daa73a6a954817d5",
)

maven_jar(
    name = "com_maxmind_db_maxmind_db",
    artifact = "com.maxmind.db:maxmind-db:0.3.1",
    sha1 = "641a7e37d3c67b10f344ed041dd23221aebf63ec",
)

maven_jar(
    name = "org_aspectj_aspectjrt",
    artifact = "org.aspectj:aspectjrt:1.8.2",
    sha1 = "7dede91c0c36b1265fc99c55283b90ed2be144c8",
)

maven_jar(
    name = "javax_activation_activation",
    artifact = "javax.activation:activation:1.1",
    sha1 = "e6cb541461c2834bdea3eb920f1884d1eb508b50",
)

maven_jar(
    name = "org_apache_xmlgraphics_batik_ext",
    artifact = "org.apache.xmlgraphics:batik-ext:1.7",
    sha1 = "4784302b44a0336166fef6153a5e3d73e861aecc",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_servlet",
    artifact = "org.eclipse.jetty:jetty-servlet:9.0.7.v20131107",
    sha1 = "f7d8ce6ecb2318b906ba4df1b8625ab2b34e305b",
)

maven_jar(
    name = "org_codehaus_jackson_jackson_core_asl",
    artifact = "org.codehaus.jackson:jackson-core-asl:1.8.9",
    sha1 = "99be07ca979279674ae5b8a886669fb8da491d9d",
)

maven_jar(
    name = "org_pojava_pojava",
    artifact = "org.pojava:pojava:2.8.1",
    sha1 = "4b9b3afd3c58a6b3eb91c50a4cae1bf58e3e7c73",
)

maven_jar(
    name = "net_java_dev_jna_platform",
    artifact = "net.java.dev.jna:jna-platform:5.3.1",
    sha1 = "925049dd00b3def5ab561709a56b96055fa67011",
)

maven_jar(
    name = "com_google_inject_extensions_guice_multibindings",
    artifact = "com.google.inject.extensions:guice-multibindings:4.1.0",
    sha1 = "3b27257997ac51b0f8d19676f1ea170427e86d51",
)

maven_jar(
    name = "com_fasterxml_jackson_core_jackson_databind",
    artifact = "com.fasterxml.jackson.core:jackson-databind:2.9.9",
    sha1 = "d6eb9817d9c7289a91f043ac5ee02a6b3cc86238",
)

maven_jar(
    name = "xmlpull_xmlpull",
    artifact = "xmlpull:xmlpull:1.1.3.1",
    sha1 = "2b8e230d2ab644e4ecaa94db7cdedbc40c805dfa",
)

maven_jar(
    name = "com_fasterxml_jackson_core_jackson_core",
    artifact = "com.fasterxml.jackson.core:jackson-core:2.9.9",
    sha1 = "bfff5af9fb8347d26bbb7959cb9b4fe9a2b0ca5e",
)

maven_jar(
    name = "org_apache_lucene_lucene_core",
    artifact = "org.apache.lucene:lucene-core:4.4.0",
    sha1 = "a9a0b553d5f2444aea3340b22753ea4bbddaa0af",
)

maven_jar(
    name = "org_apache_httpcomponents_httpmime",
    artifact = "org.apache.httpcomponents:httpmime:4.2.2",
    sha1 = "817d56b6f7c8af1fc90b5dbb78fb1125180fc3f3",
)

maven_jar(
    name = "org_apache_curator_curator_client",
    artifact = "org.apache.curator:curator-client:4.0.0",
    sha1 = "9a9ced5171f3fa0cd18296d34165234a8d3b7a94",
)

maven_jar(
    name = "ch_qos_logback_logback_classic",
    artifact = "ch.qos.logback:logback-classic:1.1.11",
    sha1 = "ccedfbacef4a6515d2983e3f89ed753d5d4fb665",
)

maven_jar(
    name = "ch_qos_logback_contrib_logback_json_classic",
    artifact = "ch.qos.logback.contrib:logback-json-classic:jar:0.1.5",
    sha1 = "f7fd4e747a9b0c50fc4f71b0055d5bea64dc05c3",
)

maven_jar(
    name = "ch_qos_logback_contrib_logback_jackson",
    artifact = "ch.qos.logback.contrib:logback-jackson:jar:0.1.5",
    sha1 = "0e8b202a23691048a01e6322dd040f75e08e9ca2",
)

maven_jar(
    name = "ch_qos_logback_contrib_logback_json_core",
    artifact = "ch.qos.logback.contrib:logback-json-core:jar:0.1.5",
    sha1 = "90bdb547819957fc940188f5830b7b59375f6fdd",
)

maven_jar(
    name = "com_codahale_metrics_metrics_jvm",
    artifact = "com.codahale.metrics:metrics-jvm:3.0.2",
    sha1 = "d0c6032905a3c6793c3ddcfade79e2b5f3ec1e25",
)

maven_jar(
    name = "org_apache_xmlgraphics_batik_css",
    artifact = "org.apache.xmlgraphics:batik-css:1.7",
    sha1 = "e6bb5c85753331534593f33fb9236acb41a0ab79",
)

maven_jar(
    name = "log4j_log4j",
    artifact = "log4j:log4j:1.2.14",
    sha1 = "03b254c872b95141751f414e353a25c2ac261b51",
)

maven_jar(
    name = "jline_jline",
    artifact = "jline:jline:0.9.94",
    sha1 = "99a18e9a44834afdebc467294e1138364c207402",
)

maven_jar(
    name = "com_codahale_metrics_metrics_core",
    artifact = "com.codahale.metrics:metrics-core:3.0.2",
    sha1 = "c6a7fb32776e984b64ff1a548e3044238ea5a931",
)

maven_jar(
    name = "javax_transaction_jta",
    artifact = "javax.transaction:jta:1.1",
    sha1 = "2ca09f0b36ca7d71b762e14ea2ff09d5eac57558",
)

maven_jar(
    name = "xpp3_xpp3_min",
    artifact = "xpp3:xpp3_min:1.1.4c",
    sha1 = "19d4e90b43059058f6e056f794f0ea4030d60b86",
)

maven_jar(
    name = "com_fasterxml_jackson_module_jackson_module_jaxb_annotations",
    artifact = "com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.9.9",
    sha1 = "52fb643de81a60839750013a520f26b6259ddeff",
)

maven_jar(
    name = "io_prometheus_simpleclient_hotspot",
    artifact = "io.prometheus:simpleclient_hotspot:0.0.19",
    sha1 = "4cd4d60a9f06922f23e589f9cf520cd1d6989aad",
)

maven_jar(
    name = "com_sun_jersey_jersey_server",
    artifact = "com.sun.jersey:jersey-server:1.18.1",
    sha1 = "6e4e4fb491ea360a0f4d672729224cbbb9cb456d",
)

maven_jar(
    name = "org_springframework_spring_jdbc",
    artifact = "org.springframework:spring-jdbc:4.3.7.RELEASE",
    sha1 = "305c8db0f9552948aec093528cd01393cc98a646",
)

maven_jar(
    name = "org_apache_lucene_lucene_join",
    artifact = "org.apache.lucene:lucene-join:4.4.0",
    sha1 = "ccf90d0082081f4fc2b0799bdbf786912bbb2284",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_codec",
    artifact = "io.netty:netty-codec:4.1.38.Final",
    sha1 = "ccfbdfc727cbf702350572a0b12fe92185ebf162",
)

maven_jar(
    name = "javax_el_javax_el_api",
    artifact = "javax.el:javax.el-api:2.2.5",
    sha1 = "370140e991eefb212a6d6baedbce585f00ef76e0",
)

maven_jar(
    name = "org_bouncycastle_bcpkix_jdk15on",
    artifact = "org.bouncycastle:bcpkix-jdk15on:1.59",
    sha1 = "9cef0aab8a4bb849a8476c058ce3ff302aba3fff",
)

maven_jar(
    name = "com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
    artifact = "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.9.9",
    sha1 = "7deb5d0d335265ace2a9048c8d6e203b588a1724",
)

maven_jar(
    name = "org_slf4j_slf4j_api",
    artifact = "org.slf4j:slf4j-api:1.7.21",
    sha1 = "139535a69a4239db087de9bab0bee568bf8e0b70",
)

maven_jar(
    name = "org_apache_logging_log4j_log4j_slf4j",
    artifact = "org.apache.logging.log4j:log4j-slf4j-impl:jar:2.11.1",
    sha1 = "4b41b53a3a2d299ce381a69d165381ca19f62912",
)

maven_jar(
    name = "com_thoughtworks_xstream_xstream",
    artifact = "com.thoughtworks.xstream:xstream:1.4.4",
    sha1 = "488e9e4a47afc81d2b2dec3c3eb3a4d0f10fe105",
)

maven_jar(
    name = "com_google_code_gson_gson",
    artifact = "com.google.code.gson:gson:2.2.2",
    sha1 = "1f96456ca233dec780aa224bff076d8e8bca3908",
)

maven_jar(
    name = "xalan_serializer",
    artifact = "xalan:serializer:2.7.1",
    sha1 = "4b4b18df434451249bb65a63f2fb69e215a6a020",
)

maven_jar(
    name = "com_fasterxml_jackson_core_jackson_annotations",
    artifact = "com.fasterxml.jackson.core:jackson-annotations:2.9.9",
    sha1 = "2ea299c145207161c212e28abbc8f513fa245940",
)

maven_jar(
    name = "org_apache_httpcomponents_httpclient",
    artifact = "org.apache.httpcomponents:httpclient:4.3.4",
    sha1 = "a9a1fef2faefed639ee0d0fba5b3b8e4eb2ff2d8",
)

maven_jar(
    name = "org_codehaus_woodstox_stax2_api",
    artifact = "org.codehaus.woodstox:stax2-api:3.1.1",
    sha1 = "0466eab062e9d1a3ce2c4631b6d09b5e5c0cbd1b",
)

maven_jar(
    name = "commons_httpclient_commons_httpclient",
    artifact = "commons-httpclient:commons-httpclient:3.1",
    sha1 = "964cd74171f427720480efdec40a7c7f6e58426a",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_websocket",
    artifact = "org.eclipse.jetty:jetty-websocket:8.1.7.v20120910",
    sha1 = "eb4b9d23ef55cfe271383656a99013840baefba0",
)

maven_jar(
    name = "org_springframework_spring_beans",
    artifact = "org.springframework:spring-beans:4.3.7.RELEASE",
    sha1 = "2de9f59f3202965438f3a02057d6ad8274636044",
)

maven_jar(
    name = "org_apache_commons_commons_math",
    artifact = "org.apache.commons:commons-math:2.1",
    sha1 = "b3c4bdc2778ddccceb8da2acec3e37bfa41303e9",
)

maven_jar(
    name = "org_apache_mahout_commons_commons_cli",
    artifact = "org.apache.mahout.commons:commons-cli:2.0-mahout",
    sha1 = "f1df8dc67ae086159ac66bd92c0bb54254776a93",
)

maven_jar(
    name = "org_apache_lucene_lucene_sandbox",
    artifact = "org.apache.lucene:lucene-sandbox:4.4.0",
    sha1 = "aef76ad9e28bd2f09c6995ce628b1753c5043a23",
)

maven_jar(
    name = "c3p0_c3p0",
    artifact = "c3p0:c3p0:0.9.1.1",
    sha1 = "302704f30c6e7abb7a0457f7771739e03c973e80",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_handler_proxy",
    artifact = "io.netty:netty-handler-proxy:4.1.38.Final",
    sha1 = "dbb09abb0c9c494cb651234eed428189eb730872",
)

maven_jar(
    name = "io_dropwizard_dropwizard_client",
    artifact = "io.dropwizard:dropwizard-client:0.7.1",
    sha1 = "7cef9884395e90a4dc627e643f84b48a2ff6fb30",
)

maven_jar(
    name = "org_slf4j_jul_to_slf4j",
    artifact = "org.slf4j:jul-to-slf4j:1.7.6",
    sha1 = "322e2af1694ccc75d33f4d11216c852121d8fefd",
)

maven_jar(
    name = "commons_configuration_commons_configuration",
    artifact = "commons-configuration:commons-configuration:1.6",
    sha1 = "32cadde23955d7681b0d94a2715846d20b425235",
)

maven_jar(
    name = "org_apache_lucene_lucene_memory",
    artifact = "org.apache.lucene:lucene-memory:4.4.0",
    sha1 = "dfc552ca9d373bf05d74a6f85fc55b24cfdf0e8c",
)

maven_jar(
    name = "jfree_jcommon",
    artifact = "jfree:jcommon:1.0.0",
    sha1 = "25b1bfc1bb224a270e30cc8c19c4b8f63108ada0",
)

maven_jar(
    name = "org_apache_lucene_lucene_misc",
    artifact = "org.apache.lucene:lucene-misc:4.4.0",
    sha1 = "db1d2054b43f189f361888c84eb63df0023de9e1",
)

maven_jar(
    name = "io_dropwizard_dropwizard_jetty",
    artifact = "io.dropwizard:dropwizard-jetty:0.7.1",
    sha1 = "269303642030a9e093b4cc19e8036e174113a04a",
)

maven_jar(
    name = "io_dropwizard_dropwizard_logging",
    artifact = "io.dropwizard:dropwizard-logging:0.7.1",
    sha1 = "039d41453b0cb37c4c6239566ccb52787a68d270",
)

maven_jar(
    name = "org_xerial_snappy_snappy_java",
    artifact = "org.xerial.snappy:snappy-java:1.0.5-M2",
    sha1 = "5b015647886ef85a6747091461099dfba944864f",
)

maven_jar(
    name = "org_jsoup_jsoup",
    artifact = "org.jsoup:jsoup:1.7.2",
    sha1 = "d7e275ba05aa380ca254f72d0c0ffebaedc3adcf",
)

maven_jar(
    name = "oauth_signpost_signpost_core",
    artifact = "oauth.signpost:signpost-core:1.2.1.1",
    sha1 = "d1b39a438178bc885d724458de64cee33f1932a6",
)

maven_jar(
    name = "com_fasterxml_jackson_datatype_jackson_datatype_guava",
    artifact = "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.9.9",
    sha1 = "8ed2f11606b6f37c087a090e333b19273f6f48cb",
)

maven_jar(
    name = "net_java_dev_jna_jna",
    artifact = "net.java.dev.jna:jna:5.3.1",
    sha1 = "6eb9d07456c56b9c2560722e90382252f0f98405",
)

maven_jar(
    name = "io_dropwizard_dropwizard_validation",
    artifact = "io.dropwizard:dropwizard-validation:0.7.1",
    sha1 = "59a73cdcab2489b6dd342926bb4250e9c62e6938",
)

maven_jar(
    name = "eu_geekplace_javapinning_java_pinning_jar",
    artifact = "eu.geekplace.javapinning:java-pinning-jar:1.0.1",
    sha1 = "bdd2809fdc4b67c4fbcdaede96355825b57febcf",
)

maven_jar(
    name = "javax_inject_javax_inject",
    artifact = "javax.inject:javax.inject:1",
    sha1 = "6975da39a7040257bd51d21a231b76c915872d38",
)

maven_jar(
    name = "org_seleniumhq_selenium_selenium_server",
    artifact = "org.seleniumhq.selenium:selenium-server:2.31.0",
    sha1 = "14996f8c7dc90e6fd93e221089bc5f4267ce7fa9",
)

maven_jar(
    name = "org_apache_lucene_lucene_highlighter",
    artifact = "org.apache.lucene:lucene-highlighter:4.4.0",
    sha1 = "c55f402683388c0a71a1dfaaff198873dfe5b1e4",
)

maven_jar(
    name = "org_apache_commons_commons_collections4",
    artifact = "org.apache.commons:commons-collections4:4.0",
    sha1 = "da217367fd25e88df52ba79e47658d4cf928b0d1",
)

maven_jar(
    name = "javax_xml_bind_jaxb_api",
    artifact = "javax.xml.bind:jaxb-api:2.2.7",
    sha1 = "2f51c4bb4724ea408096ee9100ff2827e07e5b7c",
)

maven_jar(
    name = "org_apache_kafka_kafka_clients",
    artifact = "org.apache.kafka:kafka-clients:0.11.0.1",
    sha1 = "03145da6edad54b6f8fe5e5f2e17bf54775baff2",
)

maven_jar(
    name = "org_apache_kafka_kafka_streams",
    artifact = "org.apache.kafka:kafka-streams:0.11.0.1",
    sha1 = "9fe665396cfd98ce54e0862b4535962bfcf84e52",
)

maven_jar(
    name = "org_apache_kafka_connect_json",
    artifact = "org.apache.kafka:connect-json:0.11.0.1",
    sha1 = "64532d1861ede38a7a20239e1f71bb08175385ac",
)

maven_jar(
    name = "org_apache_kafka_connect_api",
    artifact = "org.apache.kafka:connect-api:0.11.0.1",
    sha1 = "617c4de68553a3114151405a9282b7cad29c38b4",
)

maven_jar(
    name = "org_glassfish_web_javax_el",
    artifact = "org.glassfish.web:javax.el:2.2.6",
    sha1 = "0232abfe1f919127e9cfcd6e4e2c6324bb394535",
)

maven_jar(
    name = "org_springframework_spring_context",
    artifact = "org.springframework:spring-context:4.3.7.RELEASE",
    sha1 = "34b66b0b7910122ef95ba4fff6da9238ef80a5de",
)

maven_jar(
    name = "dom4j_dom4j",
    artifact = "dom4j:dom4j:1.6.1",
    sha1 = "5d3ccc056b6f056dbf0dddfdf43894b9065a8f94",
)

maven_jar(
    name = "org_springframework_spring_aop",
    artifact = "org.springframework:spring-aop:4.3.7.RELEASE",
    sha1 = "3f243d685e4a8a78a0c291445c6d85560ec4d339",
)

maven_jar(
    name = "net_sourceforge_htmlunit_htmlunit",
    artifact = "net.sourceforge.htmlunit:htmlunit:2.11",
    sha1 = "40e8c4dabf8ef3371c4d42ed2fe95baa882adda4",
)

maven_jar(
    name = "com_fasterxml_jackson_dataformat_jackson_dataformat_smile",
    artifact = "com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.9.9",
    sha1 = "85749406c69b08945d6059db679cc66990340ebc",
)

maven_jar(
    name = "io_dropwizard_dropwizard_metrics",
    artifact = "io.dropwizard:dropwizard-metrics:0.7.1",
    sha1 = "6ca1d7d1d1d1bcf7c803a127e0c6696d1c98fdb1",
)

maven_jar(
    name = "oauth_signpost_signpost_commonshttp4",
    artifact = "oauth.signpost:signpost-commonshttp4:1.2",
    sha1 = "1054da42a9c8e2acfbdf875cecebec047557a83f",
)

maven_jar(
    name = "io_dropwizard_dropwizard_jackson",
    artifact = "io.dropwizard:dropwizard-jackson:0.7.1",
    sha1 = "6bc655d16acdb556001b1d677fab6c69f7ba296a",
)

maven_jar(
    name = "org_hibernate_hibernate_commons_annotations",
    artifact = "org.hibernate:hibernate-commons-annotations:3.2.0.Final",
    sha1 = "ce990611448fc2865469e3b68d2fe76b050e3c4f",
)

maven_jar(
    name = "io_dropwizard_dropwizard_util",
    artifact = "io.dropwizard:dropwizard-util:0.7.1",
    sha1 = "49c3b2764b4f6ad1a97ae7148ac8a640488b0b29",
)

maven_jar(
    name = "com_codahale_metrics_metrics_jetty9",
    artifact = "com.codahale.metrics:metrics-jetty9:3.0.2",
    sha1 = "39cc25ad4db6320f6a97f755ebe00ed5def71b9a",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_util",
    artifact = "org.eclipse.jetty:jetty-util:9.0.7.v20131107",
    sha1 = "93a606c83b047e8855eb3af68c335e60fa757367",
)

maven_jar(
    name = "org_json_json",
    artifact = "org.json:json:20080701",
    sha1 = "d652f102185530c93b66158b1859f35d45687258",
)

maven_jar(
    name = "org_elasticsearch_elasticsearch",
    artifact = "org.elasticsearch:elasticsearch:0.90.5",
    sha1 = "f8d354d47fa4429e08fd969720f131bd27312c3f",
)

maven_jar(
    name = "io_dropwizard_dropwizard_configuration",
    artifact = "io.dropwizard:dropwizard-configuration:0.7.1",
    sha1 = "161539b50579dffbcd00eff796adc2020c02a6f9",
)

maven_jar(
    name = "com_codahale_metrics_metrics_annotation",
    artifact = "com.codahale.metrics:metrics-annotation:3.0.2",
    sha1 = "49cee8ba1d76f67c59146546448fc0f3eef32b75",
)

maven_jar(
    name = "com_googlecode_gettext_commons_gettext_commons",
    artifact = "com.googlecode.gettext-commons:gettext-commons:0.9.8",
    sha1 = "20e498b37fcced2f3fa273df2fae169e6b4e8061",
)

maven_jar(
    name = "mysql_mysql_connector_java",
    artifact = "mysql:mysql-connector-java:5.1.42",
    sha1 = "80a448a3ec2178b649bb2e3cb3610fab06e11669",
)

maven_jar(
    name = "com_fasterxml_jackson_datatype_jackson_datatype_joda",
    artifact = "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.9.9",
    sha1 = "a69b7eda6d0c422567c3ef9187510daeb97ef952",
)

maven_jar(
    name = "org_yaml_snakeyaml",
    artifact = "org.yaml:snakeyaml:1.13",
    sha1 = "73cbb494a912866c4c831a178c3a2a9169f4eaad",
)

maven_jar(
    name = "antlr_antlr",
    artifact = "antlr:antlr:2.7.6",
    sha1 = "cf4f67dae5df4f9932ae7810f4548ef3e14dd35e",
)

maven_jar(
    name = "javax_xml_stream_stax_api",
    artifact = "javax.xml.stream:stax-api:1.0-2",
    sha1 = "d6337b0de8b25e53e81b922352fbea9f9f57ba0b",
)

maven_jar(
    name = "commons_io_commons_io",
    artifact = "commons-io:commons-io:2.5",
    sha1 = "2852e6e05fbb95076fc091f6d1780f1f8fe35e0f",
)

maven_jar(
    name = "net_sourceforge_nekohtml_nekohtml",
    artifact = "net.sourceforge.nekohtml:nekohtml:1.9.17",
    sha1 = "39a870b0ea4cb0d2a3015c1ab569d17d83122d55",
)

maven_jar(
    name = "commons_codec_commons_codec",
    artifact = "commons-codec:commons-codec:1.6",
    sha1 = "b7f0fc8f61ecadeb3695f0b9464755eee44374d4",
)

maven_jar(
    name = "org_jboss_logging_jboss_logging",
    artifact = "org.jboss.logging:jboss-logging:3.1.3.GA",
    sha1 = "64499e907f19e5e1b3fdc02f81440c1832fe3545",
)

maven_jar(
    name = "io_prometheus_simpleclient_common",
    artifact = "io.prometheus:simpleclient_common:0.0.19",
    sha1 = "aa0d4a87c02e71924c913fbb4629b7ca5966a5ff",
)

maven_jar(
    name = "com_sun_jersey_jersey_core",
    artifact = "com.sun.jersey:jersey-core:1.18.1",
    sha1 = "4f97b0f85a881d3b3478f99a17df7bd258b9d626",
)

maven_jar(
    name = "xml_apis_xml_apis",
    artifact = "xml-apis:xml-apis:1.0.b2",
    sha1 = "3136ca936f64c9d68529f048c2618bd356bf85c9",
)

maven_jar(
    name = "asm_asm",
    artifact = "asm:asm:3.1",
    sha1 = "c157def142714c544bdea2e6144645702adf7097",
)

maven_jar(
    name = "io_dropwizard_dropwizard_lifecycle",
    artifact = "io.dropwizard:dropwizard-lifecycle:0.7.1",
    sha1 = "12c282d9682628d7c33654e0d62f1dbf2a883d20",
)

maven_jar(
    name = "org_apache_velocity_velocity",
    artifact = "org.apache.velocity:velocity:1.7",
    sha1 = "2ceb567b8f3f21118ecdec129fe1271dbc09aa7a",
)

maven_jar(
    name = "com_fasterxml_jackson_module_jackson_module_afterburner",
    artifact = "com.fasterxml.jackson.module:jackson-module-afterburner:2.9.9",
    sha1 = "89ac043b711248ba512b98493d2266328d1f1045",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_common",
    artifact = "io.netty:netty-common:4.1.38.Final",
    sha1 = "6f8aae763f743d91fb1ba1e9011dae0ef4f6ff34",
)

maven_jar(
    name = "org_codehaus_plexus_plexus_utils",
    artifact = "org.codehaus.plexus:plexus-utils:3.0.17",
    sha1 = "7b86f7a4ceffc8eadbb1a84207134af776f7cb95",
)

maven_jar(
    name = "org_apache_lucene_lucene_queryparser",
    artifact = "org.apache.lucene:lucene-queryparser:4.4.0",
    sha1 = "e2fca26d9c64f3aad7b8a3461dbab14782107a06",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_servlets",
    artifact = "org.eclipse.jetty:jetty-servlets:9.0.7.v20131107",
    sha1 = "bffeae9b9f75f53d3e5dc1bfd56725f67f2f67c0",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_handler",
    artifact = "io.netty:netty-handler:4.1.38.Final",
    sha1 = "ebf1f2bd0dad5e16aa1fc48d32e5dbe507b38d53",
)

maven_jar(
    name = "org_apache_lucene_lucene_codecs",
    artifact = "org.apache.lucene:lucene-codecs:4.4.0",
    sha1 = "1acc38b44825a4874233d4923f3763bae0ad9c35",
)

maven_jar(
    name = "org_apache_mahout_mahout_math",
    artifact = "org.apache.mahout:mahout-math:0.9",
    sha1 = "8f94abb3e30b2385f10b8d856f2c723b4743cbc7",
)

maven_jar(
    name = "xalan_xalan",
    artifact = "xalan:xalan:2.7.1",
    sha1 = "75f1d83ce27bab5f29fff034fc74aa9f7266f22a",
)

maven_jar(
    name = "org_modelmapper_modelmapper",
    artifact = "org.modelmapper:modelmapper:1.1.0",
    sha1 = "98b2cbf292dad9e284124e5d4c1f97771d9333a3",
)

maven_jar(
    name = "com_fasterxml_jackson_dataformat_jackson_dataformat_yaml",
    artifact = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9",
    sha1 = "0ccc00ed13e4d74f9c5cc30465b6fc4fe5ce5473",
)

maven_jar(
    name = "org_springframework_security_core",
    artifact = "org.springframework.security:spring-security-core:4.2.3.RELEASE",
    sha1 = "5c0e47a47036c94d6fdd02696bf52be6d1adbd4d",
)

maven_jar(
    name = "org_springframework_spring_core",
    artifact = "org.springframework:spring-core:4.3.7.RELEASE",
    sha1 = "54fa2db94cc7222edc90ec71354e47cd1dc07f7b",
)

maven_jar(
    name = "org_springframework_spring_tx",
    artifact = "org.springframework:spring-tx:4.3.7.RELEASE",
    sha1 = "b761cc783e49b5aa998ac63a721495a9f0f69f9c",
)

maven_jar(
    name = "com_fasterxml_jackson_dataformat_jackson_dataformat_xml",
    artifact = "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.9.9",
    sha1 = "4b8a210f1102307ec66028f744a1ede73a40ed2d",
)

maven_jar(
    name = "org_apache_curator_curator_recipes",
    artifact = "org.apache.curator:curator-recipes:4.0.0",
    sha1 = "46ca001305a74a277d8a42f377bb7c901c0423bf",
)

maven_jar(
    name = "org_apache_httpcomponents_httpcore",
    artifact = "org.apache.httpcomponents:httpcore:4.3.2",
    sha1 = "31fbbff1ddbf98f3aa7377c94d33b0447c646b6e",
)

maven_jar(
    name = "cglib_cglib_nodep",
    artifact = "cglib:cglib-nodep:2.1_3",
    sha1 = "58d3be5953547c0019e5704d6ed4ffda3b0c7c66",
)

maven_jar(
    name = "org_hibernate_hibernate_core",
    artifact = "org.hibernate:hibernate-core:3.5.4-Final",
    sha1 = "3f2e17bda96d7bffc6ec72c670ed442d5e9b0063",
)

maven_jar(
    name = "org_apache_zookeeper_zookeeper",
    artifact = "org.apache.zookeeper:zookeeper:3.5.3-beta",
    sha1 = "63ffc92b50e1da1b43d7728daeed72035b53e567",
)

maven_jar(
    name = "io_swagger_swagger_annotations",
    artifact = "io.swagger:swagger-annotations:1.5.9",
    sha1 = "0598403e3d21da08f8e46efb9f2b6d7b1bc0046d",
)

maven_jar(
    name = "io_swagger_swagger_core",
    artifact = "io.swagger:swagger-core:1.5.9",
    sha1 = "17338eed33903397a508e92ff6f98de53f99350e",
)

maven_jar(
    name = "io_swagger_swagger_jaxrs",
    artifact = "io.swagger:swagger-jaxrs:1.5.9",
    sha1 = "8963cb24f7c1bdc76604919b30cd6ae7d5f660d3",
)

maven_jar(
    name = "io_swagger_swagger_models",
    artifact = "io.swagger:swagger-models:1.5.9",
    sha1 = "7cc6e2b63619d826f9da4203630ab7add866a473",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_io",
    artifact = "org.eclipse.jetty:jetty-io:9.0.7.v20131107",
    sha1 = "512e9d2e088ae7c70c4a68381423cb68b9ed42d6",
)

maven_jar(
    name = "net_sourceforge_cssparser_cssparser",
    artifact = "net.sourceforge.cssparser:cssparser:0.9.8",
    sha1 = "f05d7c249dfe8b884e72d614531630a9992bb037",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_codec_socks",
    artifact = "io.netty:netty-codec-socks:4.1.38.Final",
    sha1 = "9a6b2c27383061ac9b6de10e6f0f81a7283216f5",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_buffer",
    artifact = "io.netty:netty-buffer:4.1.38.Final",
    sha1 = "d16cf15d29c409987cecde77407fbb6f1e16d262",
)

maven_jar(
    name = "ch_qos_logback_logback_core",
    artifact = "ch.qos.logback:logback-core:1.1.11",
    sha1 = "88b8df40340eed549fb07e2613879bf6b006704d",
)

maven_jar(
    name = "org_apache_lucene_lucene_spatial",
    artifact = "org.apache.lucene:lucene-spatial:4.4.0",
    sha1 = "a17b2d60f82dbff1ee67c59a5a1e60a3916ee1fb",
)

maven_jar(
    name = "com_codahale_metrics_metrics_jersey",
    artifact = "com.codahale.metrics:metrics-jersey:3.0.2",
    sha1 = "6ece3e06de45ee7a7e73b66ca36862f27ca88717",
)

maven_jar(
    name = "org_apache_xmlgraphics_batik_util",
    artifact = "org.apache.xmlgraphics:batik-util:1.7",
    sha1 = "5c4dd0dd9a86a2fba2c6ea26fb62b32b21b2a61e",
)

maven_jar(
    name = "io_prometheus_simpleclient",
    artifact = "io.prometheus:simpleclient:0.0.19",
    sha1 = "c1424b444a7ec61e056a180d52470ff397bc428d",
)

maven_jar(
    name = "com_datastax_cassandra_cassandra_driver_core",
    artifact = "com.datastax.cassandra:cassandra-driver-core:3.4.0",
    sha1 = "14cca584732ead9e9274716963032bfff570f787",
)

maven_jar(
    name = "com_spatial4j_spatial4j",
    artifact = "com.spatial4j:spatial4j:0.3",
    sha1 = "2fd58d1de64553db427ae4fbc1a6691adea460e2",
)

maven_jar(
    name = "com_amazonaws_jmespath_java",
    artifact = "com.amazonaws:jmespath-java:1.11.381",
    sha1 = "c428e44fa35032bbc89c6aaab15f1f3857c2afbe",
)

maven_jar(
    name = "com_amazonaws_aws_java_sdk_code_generator",
    artifact = "com.amazonaws:aws-java-sdk-code-generator:1.11.381",
    sha1 = "d8ed76e95c0313e3b9898c7cc5cc40b935d0c32b",
)

maven_jar(
    name = "com_amazonaws_aws_java_sdk_sqs",
    artifact = "com.amazonaws:aws-java-sdk-sqs:1.11.381",
    sha1 = "4ea1ad090c04fde806eb4fa24b10fa5748900ef1",
)

maven_jar(
    name = "com_amazonaws_aws_java_sdk_s3",
    artifact = "com.amazonaws:aws-java-sdk-s3:1.11.381",
    sha1 = "a2600515a420a5fd08d5c1d19fdf4a8714c515e2",
)

maven_jar(
    name = "com_amazonaws_aws_java_sdk_kms",
    artifact = "com.amazonaws:aws-java-sdk-kms:1.11.381",
    sha1 = "17a06d9854f804dfcf2ec799f906f555984084c9",
)

maven_jar(
    name = "org_springframework_data_spring_data_jpa",
    artifact = "org.springframework.data:spring-data-jpa:1.11.1.RELEASE",
    sha1 = "fa362aecd78883991f57a5d64e19f34b57a2c34d",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_codec_http2",
    artifact = "io.netty:netty-codec-http2:4.1.38.Final",
    sha1 = "0dc353dd011c512d5e631a4bee517b17ed3155c1",
)

maven_jar(
    name = "org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
    artifact = "org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.0.Final",
    sha1 = "6728add2c60f6621c3a77ba71fe6f6b798d14ee0",
)

maven_jar(
    name = "org_slf4j_jcl_over_slf4j",
    artifact = "org.slf4j:jcl-over-slf4j:1.7.6",
    sha1 = "ab1648fe1dd6f1e5c2ec6d12f394672bb8c1036a",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_http",
    artifact = "org.eclipse.jetty:jetty-http:9.0.7.v20131107",
    sha1 = "67060a59b426c76a2788ea5f4e19c1d3170ac562",
)

maven_jar(
    name = "org_apache_curator_curator_x_discovery",
    artifact = "org.apache.curator:curator-x-discovery:4.0.0",
    sha1 = "dc70d7cbcfd1e9dbb21b5aa1af3661b57fb5f850",
)

maven_jar(
    name = "commons_lang_commons_lang",
    artifact = "commons-lang:commons-lang:2.6",
    sha1 = "0ce1edb914c94ebc388f086c6827e8bdeec71ac2",
)

maven_jar(
    name = "com_codahale_metrics_metrics_healthchecks",
    artifact = "com.codahale.metrics:metrics-healthchecks:3.0.2",
    sha1 = "d99c34c33eceb7bc0e23c5b63fc517b0a53871c4",
)

maven_jar(
    name = "com_codahale_metrics_metrics_servlets",
    artifact = "com.codahale.metrics:metrics-servlets:3.0.2",
    sha1 = "58f7749af5c0f66d74e23cacf1bd94b30694ee51",
)

maven_jar(
    name = "com_google_code_findbugs_jsr305",
    artifact = "com.google.code.findbugs:jsr305:3.0.0",
    sha1 = "5871fb60dc68d67da54a663c3fd636a10a532948",
)

maven_jar(
    name = "org_apache_lucene_lucene_analyzers_common",
    artifact = "org.apache.lucene:lucene-analyzers-common:4.4.0",
    sha1 = "f58f6b727293b2d4392064db8c91fdf1d0eb4ffe",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_transport",
    artifact = "io.netty:netty-transport:4.1.38.Final",
    sha1 = "cd8b612d5daa42d1be3bb3203e4857597d5db79b",
)

maven_jar(
    name = "org_apache_mahout_mahout_core",
    artifact = "org.apache.mahout:mahout-core:0.9",
    sha1 = "22a63210e10d39be2edcc01564f5d5f68f91eeb7",
)

maven_jar(
    name = "com_yubico_yubico_validation_client2",
    artifact = "com.yubico:yubico-validation-client2:2.0.1",
    sha1 = "dea29261814ff36357d4bdbf8f57832e275bc2e8",
)

maven_jar(
    name = "org_springframework_spring_expression",
    artifact = "org.springframework:spring-expression:4.3.7.RELEASE",
    sha1 = "5257b6486e43d8c05674323fea5b415d4da72f38",
)

maven_jar(
    name = "org_w3c_css_sac",
    artifact = "org.w3c.css:sac:1.3",
    sha1 = "cdb2dcb4e22b83d6b32b93095f644c3462739e82",
)

maven_jar(
    name = "mx4j_mx4j_tools",
    artifact = "mx4j:mx4j-tools:3.0.1",
    sha1 = "df853af9fe34d4eb6f849a1b5936fddfcbe67751",
)

maven_jar(
    name = "cglib_cglib",
    artifact = "cglib:cglib:2.2",
    sha1 = "97d03461dc1c04ffc636dcb2579aae7724a78ef2",
)

maven_jar(
    name = "io_dropwizard_dropwizard_servlets",
    artifact = "io.dropwizard:dropwizard-servlets:0.7.1",
    sha1 = "b9d2016bbeb2df39cc90632338923273d708b55e",
)

maven_jar(
    name = "org_apache_commons_commons_csv",
    artifact = "org.apache.commons:commons-csv:1.0",
    sha1 = "8a0796ad6541a144eb1c00b93e06fbac03a9f313",
)

maven_jar(
    name = "net_sourceforge_htmlunit_htmlunit_core_js",
    artifact = "net.sourceforge.htmlunit:htmlunit-core-js:2.11",
    sha1 = "d11718ebc89876ebe2743a1f200d21f599bc5e0f",
)

maven_jar(
    name = "org_apache_commons_commons_math3",
    artifact = "org.apache.commons:commons-math3:3.2",
    sha1 = "ec2544ab27e110d2d431bdad7d538ed509b21e62",
)

maven_jar(
    name = "net_jpountz_lz4_lz4",
    artifact = "net.jpountz.lz4:lz4:1.3.0",
    sha1 = "c708bb2590c0652a642236ef45d9f99ff842a2ce",
)

maven_jar(
    name = "org_mortbay_jetty_servlet_api_2_5",
    artifact = "org.mortbay.jetty:servlet-api-2.5:6.1.9",
    sha1 = "96425fc6a410817cd4c27e65a240cb8328eee9ad",
)

maven_jar(
    name = "org_eclipse_jetty_orbit_javax_servlet",
    artifact = "org.eclipse.jetty.orbit:javax.servlet:3.0.0.v201112011016",
    sha1 = "0aaaa85845fb5c59da00193f06b8e5278d8bf3f8",
)

maven_jar(
    name = "org_webbitserver_webbit",
    artifact = "org.webbitserver:webbit:0.4.14",
    sha1 = "3bf3f17fe41fb34c4d98663957ec0795a6b6653e",
)

maven_jar(
    name = "org_hibernate_hibernate_entitymanager",
    artifact = "org.hibernate:hibernate-entitymanager:3.5.4-Final",
    sha1 = "23874caf98a2afb66581fde5b94d814c399c0063",
)

maven_jar(
    name = "xml_apis_xml_apis_ext",
    artifact = "xml-apis:xml-apis-ext:1.3.04",
    sha1 = "41a8b86b358e87f3f13cf46069721719105aff66",
)

maven_jar(
    name = "io_prometheus_simpleclient_servlet",
    artifact = "io.prometheus:simpleclient_servlet:0.0.19",
    sha1 = "f7ed8e8f32aafbe9e918f4c8fcec7f99dee9b6f9",
)

maven_jar(
    name = "com_fasterxml_classmate",
    artifact = "com.fasterxml:classmate:1.0.0",
    sha1 = "434efef28c81162b17c540e634cffa3bd9b09b4c",
)

maven_jar(
    name = "org_hibernate_hibernate_annotations",
    artifact = "org.hibernate:hibernate-annotations:3.5.4-Final",
    sha1 = "56e3be054a0d3a99c0fd99582127fb87a6911333",
)

maven_jar(
    name = "com_google_http_client_google_http_client",
    artifact = "com.google.http-client:google-http-client:1.17.0-rc",
    sha1 = "637da6cca16f4a97c4771137bce02e7c291e67af",
)

maven_jar(
    name = "org_bouncycastle_bcprov_jdk15on",
    artifact = "org.bouncycastle:bcprov-jdk15on:1.59",
    sha1 = "2507204241ab450456bdb8e8c0a8f986e418bd99",
)

maven_jar(
    name = "com_sun_jersey_jersey_servlet",
    artifact = "com.sun.jersey:jersey-servlet:1.18.1",
    sha1 = "ecec31589375845cbd42e75c32b64a1475c44bb4",
)

maven_jar(
    name = "joda_time_joda_time",
    artifact = "joda-time:joda-time:2.9.9",
    sha1 = "f7b520c458572890807d143670c9b24f4de90897",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_tcnative_boringssl_static",
    artifact = "io.netty:netty-tcnative-boringssl-static:2.0.25.Final",
    sha1 = "185980556f9f083b5339825f19c9641c6c879417",
)

maven_jar(
    name = "de_jollyday_jollyday",
    artifact = "de.jollyday:jollyday:0.4.7",
    sha1 = "aa1c57aa11494985854b8ec8d39574754db67f22",
)

maven_jar(
    name = "org_seleniumhq_selenium_jetty_repacked",
    artifact = "org.seleniumhq.selenium:jetty-repacked:7.6.1",
    sha1 = "3937008b2f7c124f52f7734eba4f6efc148799c6",
)

maven_jar(
    name = "com_sun_jersey_jersey_client",
    artifact = "com.sun.jersey:jersey-client:1.18.1",
    sha1 = "60e85d4f638fb444bcbbd9f8da83414fbdf731c3",
)

maven_jar(
    name = "org_javassist_javassist",
    artifact = "org.javassist:javassist:3.21.0-GA",
    sha1 = "598244f595db5c5fb713731eddbb1c91a58d959b",
)

maven_jar(
    name = "xerces_xercesImpl",
    artifact = "xerces:xercesImpl:2.10.0",
    sha1 = "9161654d2afe7f9063455f02ccca8e4ec2787222",
)

maven_jar(
    name = "org_hibernate_hibernate_validator",
    artifact = "org.hibernate:hibernate-validator:5.1.1.Final",
    sha1 = "2bd44618dc13c2be39231776a0edf0e1f867dedc",
)

maven_jar(
    name = "com_github_rholder_guava_retrying",
    artifact = "com.github.rholder:guava-retrying:2.0.0",
    sha1 = "974bc0a04a11cc4806f7c20a34703bd23c34e7f4",
)

maven_jar(
    name = "commons_collections_commons_collections",
    artifact = "commons-collections:commons-collections:3.1",
    sha1 = "40fb048097caeacdb11dbb33b5755854d89efdeb",
)

maven_jar(
    name = "junit_junit",
    artifact = "junit:junit:4.12",
    sha1 = "2973d150c0dc1fefe998f834810d68f278ea58ec",
)

maven_jar(
    name = "org_springframework_spring_orm",
    artifact = "org.springframework:spring-orm:4.3.7.RELEASE",
    sha1 = "d9b193994609086ea1f067af07e0af5f53303d92",
)

maven_jar(
    name = "commons_logging_commons_logging",
    artifact = "commons-logging:commons-logging:1.1.3",
    sha1 = "f6f66e966c70a83ffbdb6f17a0919eaf7c8aca7f",
)

maven_jar(
    name = "org_apache_commons_commons_exec",
    artifact = "org.apache.commons:commons-exec:1.1",
    sha1 = "07dfdf16fade726000564386825ed6d911a44ba1",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_codec_http",
    artifact = "io.netty:netty-codec-http:4.1.38.Final",
    sha1 = "4d55b3cdb74cd140d262de96987ebd369125a64c",
)

maven_jar(
    name = "org_apache_lucene_lucene_suggest",
    artifact = "org.apache.lucene:lucene-suggest:4.4.0",
    sha1 = "1e868b3a2affe2a625fc0c18b4dabba585883a90",
)

maven_jar(
    name = "net_jcip_jcip_annotations",
    artifact = "net.jcip:jcip-annotations:1.0",
    sha1 = "afba4942caaeaf46aab0b976afd57cc7c181467e",
)

maven_jar(
    name = "com_fasterxml_jackson_jaxrs_jackson_jaxrs_base",
    artifact = "com.fasterxml.jackson.jaxrs:jackson-jaxrs-base:2.9.9",
    sha1 = "fc33bfa121b746db0f9fb8f36ed2e6682a1c1dd0",
)

maven_jar(
    name = "org_springframework_data_spring_data_commons",
    artifact = "org.springframework.data:spring-data-commons:1.13.1.RELEASE",
    sha1 = "4e4257f2eb3f191613b4b000d43e8d0c3ff4457e",
)

maven_jar(
    name = "net_spy_spymemcached",
    artifact = "net.spy:spymemcached:2.9.1",
    sha1 = "a09d5c077370dca14c0c967c5fb1b1fc9d24a02d",
)

maven_jar(
    name = "com_sun_jersey_contribs_jersey_apache_client4",
    artifact = "com.sun.jersey.contribs:jersey-apache-client4:1.18.1",
    sha1 = "9dbac6cb2e05715b5b55f92187e7dc52f8c718e1",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_continuation",
    artifact = "org.eclipse.jetty:jetty-continuation:9.0.7.v20131107",
    sha1 = "4a26ae30011d933ac2c5f8d840e3374bc0d136eb",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_security",
    artifact = "org.eclipse.jetty:jetty-security:9.0.7.v20131107",
    sha1 = "023e7943c18e5c340455a40876ce5093c980c210",
)

maven_jar(
    name = "org_apache_commons_commons_lang3",
    artifact = "org.apache.commons:commons-lang3:3.4",
    sha1 = "5fe28b9518e58819180a43a850fbc0dd24b7c050",
)

maven_jar(
    name = "com_google_guava_guava",
    artifact = "com.google.guava:guava:21.0",
    sha1 = "3a3d111be1be1b745edfa7d91678a12d7ed38709",
)

maven_jar(
    name = "org_codehaus_jackson_jackson_mapper_asl",
    artifact = "org.codehaus.jackson:jackson-mapper-asl:1.8.9",
    sha1 = "e430ed24d67dfc126ee16bc23156a044950c3168",
)

maven_jar(
    name = "org_seleniumhq_selenium_selenium_support",
    artifact = "org.seleniumhq.selenium:selenium-support:2.29.0",
    sha1 = "df807afb2e64a819da0e5cfb106892cfc5536131",
)

maven_jar(
    name = "org_apache_lucene_lucene_queries",
    artifact = "org.apache.lucene:lucene-queries:4.4.0",
    sha1 = "c9010f4852345ba2a65163fdeb17b7b653e4a3c4",
)

maven_jar(
    name = "org_apache_curator_curator_framework",
    artifact = "org.apache.curator:curator-framework:4.0.1",
    sha1 = "3da85d2bda41cb43dc18c089820b67d12ba38826",
)

maven_jar(
    name = "org_iban4j_iban4j",
    artifact = "org.iban4j:iban4j:3.1.0",
    sha1 = "ee5e0ee22de269a3ec2785c17a5ff2beecbb76ae",
)

maven_jar(
    name = "com_lambdaworks_scrypt",
    artifact = "com.lambdaworks:scrypt:1.3.2",
    sha1 = "b144d80b2b915a900730dd156ae749a88c0e2555",
)

maven_jar(
    name = "com_codahale_metrics_metrics_json",
    artifact = "com.codahale.metrics:metrics-json:3.0.2",
    sha1 = "46198fe0284104519b60ff2ad49f71c98ba942f2",
)

maven_jar(
    name = "hsqldb_hsqldb",
    artifact = "hsqldb:hsqldb:1.8.0.10",
    sha1 = "7e9978fdb754bce5fcd5161133e7734ecb683036",
)

maven_jar(
    name = "commons_net_commons_net",
    artifact = "commons-net:commons-net:1.4.1",
    sha1 = "abb932adb2c10790c1eaa4365d3ac2a1ac7cb700",
)

maven_jar(
    name = "xmlenc_xmlenc",
    artifact = "xmlenc:xmlenc:0.52",
    sha1 = "d82554efbe65906d83b3d97bd7509289e9db561a",
)

maven_jar(
    name = "tomcat_jasper_runtime",
    artifact = "tomcat:jasper-runtime:5.5.12",
    sha1 = "f3a50a55414655b9843f5a089923ea83d49dc55e",
)

maven_jar(
    name = "tomcat_jasper_compiler",
    artifact = "tomcat:jasper-compiler:5.5.12",
    sha1 = "c594866c64565344c0e7bdc9bf4fee70290c4dd5",
)

maven_jar(
    name = "org_codehaus_jackson_jackson_xc",
    artifact = "org.codehaus.jackson:jackson-xc:1.7.1",
    sha1 = "5eb6018a46439ae23afa3699005277ca2a0f7b47",
)

maven_jar(
    name = "com_sun_jersey_jersey_json",
    artifact = "com.sun.jersey:jersey-json:1.8",
    sha1 = "825621478fec59983106efaa032c679f925b4eff",
)

maven_jar(
    name = "stax_stax_api",
    artifact = "stax:stax-api:1.0.1",
    sha1 = "49c100caf72d658aca8e58bd74a4ba90fa2b0d70",
)

maven_jar(
    name = "oro_oro",
    artifact = "oro:oro:2.0.8",
    sha1 = "5592374f834645c4ae250f4c9fbb314c9369d698",
)

maven_jar(
    name = "org_eclipse_jdt_core",
    artifact = "org.eclipse.jdt:core:3.1.1",
    sha1 = "88c83ce444cf46d02494da37c9fa1eebc9ce9cea",
)

maven_jar(
    name = "org_codehaus_jettison_jettison",
    artifact = "org.codehaus.jettison:jettison:1.1",
    sha1 = "1a01a2a1218fcf9faa2cc2a6ced025bdea687262",
)

maven_jar(
    name = "commons_cli_commons_cli",
    artifact = "commons-cli:commons-cli:1.4",
    sha1 = "c51c00206bb913cd8612b24abd9fa98ae89719b1",
)

maven_jar(
    name = "org_codehaus_jackson_jackson_jaxrs",
    artifact = "org.codehaus.jackson:jackson-jaxrs:1.7.1",
    sha1 = "28dacb717bedc4f6edd9ca088448ef69d4dcd856",
)

maven_jar(
    name = "net_java_dev_jets3t_jets3t",
    artifact = "net.java.dev.jets3t:jets3t:0.6.1",
    sha1 = "9f95b944ccbbbeedd397e8f62e5690fd735a45e5",
)

maven_jar(
    name = "commons_el_commons_el",
    artifact = "commons-el:commons-el:1.0",
    sha1 = "1df2c042b3f2de0124750241ac6c886dbfa2cc2c",
)

maven_jar(
    name = "com_sun_xml_bind_jaxb_impl",
    artifact = "com.sun.xml.bind:jaxb-impl:2.2.3-1",
    sha1 = "56baae106392040a45a06d4a41099173425da1e6",
)

maven_jar(
    name = "ant_ant",
    artifact = "ant:ant:1.6.5",
    sha1 = "7d18faf23df1a5c3a43613952e0e8a182664564b",
)

maven_jar(
    name = "org_mockito_mockito_core",
    artifact = "org.mockito:mockito-core:2.2.22",
    sha1 = "3e50bf8784e32843a6f6d5d84ceecd8536cb979c",
)

maven_jar(
    name = "org_assertj_assertj_core",
    artifact = "org.assertj:assertj-core:2.2.0",
    sha1 = "edd59795b236afc790dd161e7e3677757b06f2e7",
)

maven_jar(
    name = "org_xmlunit_xmlunit_legacy",
    artifact = "org.xmlunit:xmlunit-legacy:2.1.1",
    sha1 = "e4d45154e0cef8334ccb7f3e0b8ebaf2596eb477",
)

maven_jar(
    name = "org_xmlunit_xmlunit_core",
    artifact = "org.xmlunit:xmlunit-core:2.1.1",
    sha1 = "94840bd83168c7de36f3779e2514d0bf4ed8c9bc",
)

maven_jar(
    name = "com_github_tomakehurst_wiremock",
    artifact = "com.github.tomakehurst:wiremock:2.1.12",
    sha1 = "898362f151219ce8eb659cb21115cca9b1c7ad48",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_xml",
    artifact = "org.eclipse.jetty:jetty-xml:9.2.13.v20150730",
    sha1 = "9e17bdfb8c25d0cd377960326b79379df3181776",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_webapp",
    artifact = "org.eclipse.jetty:jetty-webapp:9.2.13.v20150730",
    sha1 = "716b5cdea1e818cd0e36dfea791f620d49bd2d2a",
)

maven_jar(
    name = "net_minidev_asm",
    artifact = "net.minidev:asm:1.0.2",
    sha1 = "49f3068a4591b4aa6af553905ff2145685a21c2c",
)

maven_jar(
    name = "net_sf_jopt_simple_jopt_simple",
    artifact = "net.sf.jopt-simple:jopt-simple:4.9",
    sha1 = "ee9e9eaa0a35360dcfeac129ff4923215fd65904",
)

maven_jar(
    name = "net_minidev_json_smart",
    artifact = "net.minidev:json-smart:2.1.1",
    sha1 = "922d12fb1f394e2b6999ae0f7936ab13f4dffb81",
)

maven_jar(
    name = "com_jayway_jsonpath_json_path",
    artifact = "com.jayway.jsonpath:json-path:2.0.0",
    sha1 = "26b8555596b3fb9652c1ffe193fa9123945b32cc",
)

maven_jar(
    name = "com_flipkart_zjsonpatch_zjsonpatch",
    artifact = "com.flipkart.zjsonpatch:zjsonpatch:0.2.1",
    sha1 = "f3f67d52dbf2ca6edc2ae0b3ae53488110e848c9",
)

maven_jar(
    name = "pl_pragmatists_JUnitParams",
    artifact = "pl.pragmatists:JUnitParams:1.0.5",
    sha1 = "11c4fb84973ba635673e5e026abf27078aab8bd9",
)

maven_jar(
    name = "org_hamcrest_hamcrest_core",
    artifact = "org.hamcrest:hamcrest-core:1.3",
    sha1 = "42a25dc3219429f0e5d060061f71acb49bf010a0",
)

maven_jar(
    name = "org_hamcrest_hamcrest_library",
    artifact = "org.hamcrest:hamcrest-library:1.3",
    sha1 = "4785a3c21320980282f9f33d0d1264a69040538f",
)

maven_jar(
    name = "io_protostuff_protostuff_runtime",
    artifact = "io.protostuff:protostuff-runtime:1.5.1",
    sha1 = "9cb10e589b733d1eff3a14af9eaef7ce2c0292ad",
)

maven_jar(
    name = "io_protostuff_protostuff_api",
    artifact = "io.protostuff:protostuff-api:1.5.1",
    sha1 = "74ed8cbdd6e51e92c5aa1fb2166d04c3bc0a341e",
)

maven_jar(
    name = "io_protostuff_protostuff_core",
    artifact = "io.protostuff:protostuff-core:1.5.1",
    sha1 = "4da0b81402abbb86c905032e2491050c0c3b7f74",
)

maven_jar(
    name = "net_bytebuddy_byte_buddy",
    artifact = "net.bytebuddy:byte-buddy:1.10.1",
    sha1 = "11fe565e2b8b857c9dcee66e4170b97abb92530b",
)

maven_jar(
    name = "net_bytebuddy_byte_buddy_agent",
    artifact = "net.bytebuddy:byte-buddy-agent:1.10.1",
    sha1 = "9b7d95d699d08e92eb51a29b4949ae975325b40b",
)

maven_jar(
    name = "org_objenesis_objenesis",
    artifact = "org.objenesis:objenesis:2.4",
    sha1 = "2916b6c96b50c5b3ec4452ed99401db745aabb27",
)

maven_jar(
    name = "com_github_jnr_jnr_constants",
    artifact = "com.github.jnr:jnr-constants:0.9.0",
    sha1 = "6894684e17a84cd500836e82b5e6c674b4d4dda6",
)

maven_jar(
    name = "com_github_jnr_jnr_x86asm",
    artifact = "com.github.jnr:jnr-x86asm:1.0.2",
    sha1 = "006936bbd6c5b235665d87bd450f5e13b52d4b48",
)

maven_jar(
    name = "com_github_jnr_jnr_ffi",
    artifact = "com.github.jnr:jnr-ffi:2.0.7",
    sha1 = "f0968c5bb5a283ebda2df3604c2c1129d45196e3",
)

maven_jar(
    name = "org_ow2_asm_asm",
    artifact = "org.ow2.asm:asm:5.0.3",
    sha1 = "dcc2193db20e19e1feca8b1240dbbc4e190824fa",
)

maven_jar(
    name = "com_github_jnr_jnr_posix",
    artifact = "com.github.jnr:jnr-posix:3.0.27",
    sha1 = "f7441d13187d93d59656ac8f800cba3043935b59",
)

maven_jar(
    name = "com_github_jnr_jffi",
    artifact = "com.github.jnr:jffi:1.2.10",
    sha1 = "d58fdb2283456bc3f049bfbef40b592fa1aaa975",
)

# TODO: build grpc jars

maven_jar(
    name = "io_grpc_grpc_protobuf",
    artifact = "io.grpc:grpc-protobuf:%s" % GRPC_JAVA_VERSION,
    sha1 = "01428515d3aca8964dfdc4d4ba912d0fda0f41f2",
)

maven_jar(
    name = "io_grpc_grpc_auth",
    artifact = "io.grpc:grpc-auth:%s" % GRPC_JAVA_VERSION,
    sha1 = "19d71f19653d2cc786498819557431312d0dbf2d",
)

maven_jar(
    name = "io_grpc_grpc_context",
    artifact = "io.grpc:grpc-context:%s" % GRPC_JAVA_VERSION,
    sha1 = "94aedfbfeebc5a32bdfe6984289bb18abf93cf20",
)

maven_jar(
    name = "io_grpc_grpc_protobuf_lite",
    artifact = "io.grpc:grpc-protobuf-lite:%s" % GRPC_JAVA_VERSION,
    sha1 = "c030daf2f8c4185ee003e206c38e28987fe2684d",
)

maven_jar(
    name = "io_grpc_grpc_protobuf_nano",
    artifact = "io.grpc:grpc-protobuf-nano:%s" % GRPC_JAVA_NANO_VERSION,
    sha1 = "9fce4ff1563fd0176aaefb0a083b9d66a0346bd7",
)

maven_jar(
    name = "io_grpc_grpc_stub",
    artifact = "io.grpc:grpc-stub:%s" % GRPC_JAVA_VERSION,
    sha1 = "2e9e6890a7e8402a9b715ce1fad0d1827e733e49",
)

maven_jar(
    name = "io_grpc_grpc_core",
    artifact = "io.grpc:grpc-core:%s" % GRPC_JAVA_VERSION,
    sha1 = "82d0c88d65acf92fb3d66a0ee800b5da85258c39",
)

maven_jar(
    name = "io_grpc_grpc_api",
    artifact = "io.grpc:grpc-api:%s" % GRPC_JAVA_VERSION,
    sha1 = "903f250bc1d01299480e526a25cd974088699a48",
)

maven_jar(
    name = "io_grpc_grpc_netty",
    artifact = "io.grpc:grpc-netty:%s" % GRPC_JAVA_VERSION,
    sha1 = "a166b3e2abb4b47810434ee6883435b6d789b2a6",
)

maven_jar(
    name = "io_grpc_grpc_testing",
    artifact = "io.grpc:grpc-testing:%s" % GRPC_JAVA_VERSION,
    sha1 = "9397894991efc626d8b95c74691285a4468f55af",
)

maven_jar(
    name = "io_grpc_grpc_services",
    artifact = "io.grpc:grpc-services:%s" % GRPC_JAVA_VERSION,
    sha1 = "fc18ad19c48b58df090ad9f07646bc68780c3b8d",
)

maven_jar(
    name = "com_google_auth_google_auth_library_credentials",
    artifact = "com.google.auth:google-auth-library-credentials:jar:0.4.0",
    sha1 = "171da91494a1391aef13b88bd7302b29edb8d3b3",
)

maven_jar(
    name = "com_google_instrumentation_instrumentation_api",
    artifact = "com.google.instrumentation:instrumentation-api:0.4.3",
    sha1 = "41614af3429573dc02645d541638929d877945a2",
)

maven_jar(
    name = "io_takari_junit_takari_cpsuite",
    artifact = "io.takari.junit:takari-cpsuite:1.2.7",
    sha1 = "6d30ab231a73f865a3146ca4b9e3299d2f415426",
)

bind(
    name = "cpsuite",
    actual = "@io_takari_junit_takari_cpsuite//jar",
)

maven_jar(
    name = "org_reflections_reflections",
    artifact = "org.reflections:reflections:0.9.11",
    sha1 = "4c686033d918ec1727e329b7222fcb020152e32b",
)

maven_jar(
    name = "org_apache_pdfbox_pdfbox",
    artifact = "org.apache.pdfbox:pdfbox:2.0.6",
    sha1 = "68616a583c5f9b9ba72140364d15a07cd937ce0e",
)

maven_jar(
    name = "org_apache_pdfbox_fontbox",
    artifact = "org.apache.pdfbox:fontbox:2.0.0",
    sha1 = "6f762d4e1c8ea99589d30597ef3731dfdcee43e2",
)

maven_jar(
    name = "com_nimbusds_srp6a",
    artifact = "com.nimbusds:srp6a:2.0.2",
    sha1 = "fc461127a39208502518ccbe51100c315e7625e8",
)

maven_jar(
    name = "com_auth0_java_jwt",
    artifact = "com.auth0:java-jwt:3.3.0",
    sha1 = "0e180a4b31f14c2a1cf203f457fb2149d2f6c1d2",
)

maven_jar(
    name = "net_sourceforge_lept4j",
    artifact = "net.sourceforge.lept4j:lept4j:1.10.0",
    sha1 = "72153b28e8e1f0391afcc2380c41ac8e73bd599e",
)

maven_jar(
    name = "net_sourceforge_tess4j",
    artifact = "net.sourceforge.tess4j:tess4j:4.0.2",
    sha1 = "95516b133368840a0974ef5316fedd9c5e3aa635",
)

maven_jar(
    name = "com_sun_media_jai_imageio",
    artifact = "com.github.jai-imageio:jai-imageio-core:1.4.0",
    sha1 = "fb6d79b929556362a241b2f65a04e538062f0077",
)

maven_jar(
    name = "org_apache_logging_log4j_log4j_core",
    artifact = "org.apache.logging.log4j:log4j-core:2.11.1",
    sha1 = "592a48674c926b01a9a747c7831bcd82a9e6d6e4",
)

maven_jar(
    name = "org_apache_logging_log4j_log4j_api",
    artifact = "org.apache.logging.log4j:log4j-api:2.11.1",
    sha1 = "268f0fe4df3eefe052b57c87ec48517d64fb2a10",
)

maven_jar(
    name = "com_google_api_grpc_proto_google_common_protos",
    artifact = "com.google.api.grpc:proto-google-common-protos:0.1.9",
    sha1 = "3760f6a6e13c8ab070aa629876cdd183614ee877",
)

maven_jar(
    name = "com_google_protobuf_protobuf_java",
    artifact = "com.google.protobuf:protobuf-java:3.5.1",
    sha1 = "8c3492f7662fa1cbf8ca76a0f5eb1146f7725acd",
)

maven_jar(
    name = "com_google_protobuf_protobuf_java_util",
    artifact = "com.google.protobuf:protobuf-java-util:3.5.1",
    sha1 = "6e40a6a3f52455bd633aa2a0dba1a416e62b4575",
)

maven_jar(
    name = "com_google_zxing_qrcode_core",
    artifact = "com.google.zxing:core:3.3.3",
    sha1 = "b640badcc97f18867c4dfd249ef8d20ec0204c07",
)

maven_jar(
    name = "com_google_zxing_qrcode_javase",
    artifact = "com.google.zxing:javase:3.3.3",
    sha1 = "44d02048349c96eacb394af7978b3e6f1777bb02",
)

maven_jar(
    name = "io_prometheus_simpleclient_skeleton_version",
    artifact = "io.prometheus:simpleclient:0.5.0",
    sha1 = "fbbfe2300098798e3d23f93b7b14befeceacf512",
)

maven_jar(
    name = "io_prometheus_simpleclient_hotspot_skeleton_version",
    artifact = "io.prometheus:simpleclient_hotspot:0.5.0",
    sha1 = "0f341cb84d6713255b1ce46c7593eee50a35d414",
)

maven_jar(
    name = "io_prometheus_simpleclient_httpserver",
    artifact = "io.prometheus:simpleclient_httpserver:0.5.0",
    sha1 = "53fbb42b6501cee3c879ed2f8f86b64b105604aa",
)

maven_jar(
    name = "io_prometheus_simpleclient_pushgateway",
    artifact = "io.prometheus:simpleclient_pushgateway:0.5.0",
    sha1 = "92e164e6094dea346fd8f1e1a0a96f6a91c149ff",
)

maven_jar(
    name = "io_prometheus_simpleclient_common_skeleton_version",
    artifact = "io.prometheus:simpleclient_common:0.5.0",
    sha1 = "bfd93082d7cf85c0543c2ccc286b96c817d1090c",
)

maven_jar(
    name = "io_vavr_core",
    artifact = "io.vavr:vavr:0.10.0",
    sha1 = "c9f28385e6ca99f9c253c4eef879720663905329",
)

maven_jar(
    name = "io_vavr_jackson",
    artifact = "io.vavr:vavr-jackson:0.10.0",
    sha1 = "45896adac70e6e35ec5f718a9dd9ed21e5fdbdf0",
)

maven_jar(
    name = "io_vavr_match",
    artifact = "io.vavr:vavr-match:0.10.0",
    sha1 = "2088877806b1c07514a134fa10d6a7ad480cac70",
)

maven_jar(
    name = "io_vavr_test",
    artifact = "io.vavr:vavr-test:0.10.0",
    sha1 = "c4ffe88bfe1f20ff9a88e7ba5771a7faab802703",
)

maven_jar(
    name = "com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
    artifact = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.9",
    sha1 = "a33df137557793b0404a486888dbe049f7abeeeb",
)

maven_jar(
    name = "org_awaitility_awaitility",
    artifact = "org.awaitility:awaitility:4.0.1",
    sha1 = "b1b83c03c9d58c8b1aaf116b1e5365fa2ed2b572",
)

maven_jar(
    name = "io_grpc_grpc_testing_proto",
    artifact = "io.grpc:grpc-testing-proto:1.24.0",
    sha1 = "4bf1a3b690dc12ea0999643ae8e37c09e55a8c13",
)

maven_jar(
    name = "com_jcract_jzlib",
    artifact = "com.jcraft:jzlib:1.1.3",
    sha1 = "c01428efa717624f7aabf4df319939dda9646b2d",
)

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty
maven_jar(
    name = "io_netty_netty_dev_tools",
    artifact = "io.netty:netty-dev-tools:4.1.38.Final",
    sha1 = "1208cf0fb96a4faa8030c735af3410cfd04012f9",
)

maven_jar(
    name = "org_mockito3_mockito_core",
    artifact = "org.mockito:mockito-core:3.0.0",
    sha1 = "15fd0225cb1858f6922f44776b1577ac26739279",
)

maven_jar(
    name = "com_oracle_substratevm_svm",
    artifact = "com.oracle.substratevm:svm:19.0.0",
    sha1 = "e1cba96c39e75e62fd8cc50978ef7d6cfeaf39f9",
)

### === START === Java Spark dependencies
maven_jar(
    name = "com_sparkjava_spark_core",
    artifact = "com.sparkjava:spark-core:2.8.0",
    sha1 = "784ff9ba2ff8b45ef44b4cbe7a8b3e34a839a69b",
)

maven_jar(
    name = "org_eclipse_jetty_websocket_websocket_api_spark_dep",
    artifact = "org.eclipse.jetty.websocket:websocket-api:9.4.12.v20180830",
    sha1 = "97d6376f70ae6c01112325c5254e566af118bc75",
)

maven_jar(
    name = "org_eclipse_jetty_websocket_websocket_client_spark_dep",
    artifact = "org.eclipse.jetty.websocket:websocket-client:9.4.12.v20180830",
    sha1 = "75880b6a90a6eda83fdbfc20a42f23eade4b975d",
)

maven_jar(
    name = "org_eclipse_jetty_websocket_websocket_common_spark_dep",
    artifact = "org.eclipse.jetty.websocket:websocket-common:9.4.12.v20180830",
    sha1 = "33997cdafbabb3ffd6947a5c33057f967e10535b",
)

maven_jar(
    name = "org_eclipse_jetty_websocket_websocket_server_spark_dep",
    artifact = "org.eclipse.jetty.websocket:websocket-server:9.4.12.v20180830",
    sha1 = "fadf609aec6026cb25f25b6bc0b979821f849fd7",
)

maven_jar(
    name = "org_eclipse_jetty_websocket_websocket_servlet_spark_dep",
    artifact = "org.eclipse.jetty.websocket:websocket-servlet:9.4.12.v20180830",
    sha1 = "8d212616b6ea21b96152ff202c2f53fdca8b8b53",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_client_spark_dep",
    artifact = "org.eclipse.jetty:jetty-client:9.4.12.v20180830",
    sha1 = "1d329d68f31dce13135243c06013aaf6f708f7e7",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_http_spark_dep",
    artifact = "org.eclipse.jetty:jetty-http:9.4.12.v20180830",
    sha1 = "1341796dde4e16df69bca83f3e87688ba2e7d703",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_io_spark_dep",
    artifact = "org.eclipse.jetty:jetty-io:9.4.12.v20180830",
    sha1 = "e93f5adaa35a9a6a85ba130f589c5305c6ecc9e3",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_security_spark_dep",
    artifact = "org.eclipse.jetty:jetty-security:9.4.12.v20180830",
    sha1 = "299e0602a9c0b753ba232cc1c1dda72ddd9addcf",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_server_spark_dep",
    artifact = "org.eclipse.jetty:jetty-server:9.4.12.v20180830",
    sha1 = "b0f25df0d32a445fd07d5f16fff1411c16b888fa",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_servlet_spark_dep",
    artifact = "org.eclipse.jetty:jetty-servlet:9.4.12.v20180830",
    sha1 = "4c1149328eda9fa39a274262042420f66d9ffd5f",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_util_spark_dep",
    artifact = "org.eclipse.jetty:jetty-util:9.4.12.v20180830",
    sha1 = "cb4ccec9bd1fe4b10a04a0fb25d7053c1050188a",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_webapp_spark_dep",
    artifact = "org.eclipse.jetty:jetty-webapp:9.4.12.v20180830",
    sha1 = "a3e119df2da04fcf5aa290c8c35c5b310ce2dcd1",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_xml_spark_dep",
    artifact = "org.eclipse.jetty:jetty-xml:9.4.12.v20180830",
    sha1 = "e9f1874e9b5edd498f2fe7cd0904405da07cc300",
)

maven_jar(
    name = "javax_servlet_javax_servlet_api_spark_dep",
    artifact = "javax.servlet:javax.servlet-api:3.1.0",
    sha1 = "3cd63d075497751784b2fa84be59432f4905bf7c",
)

maven_jar(
    name = "com_nimbusds_nimbus_jose_jwt",
    artifact = "com.nimbusds:nimbus-jose-jwt:7.7",
    sha1 = "18590fdb64ff9443d37a9fbb2a2b1b519c6f0579",
)

maven_jar(
    name = "com_github_stephenc_jcip_annotations",
    artifact = "com.github.stephenc.jcip:jcip-annotations:1.0-1",
    sha1 = "ef31541dd28ae2cefdd17c7ebf352d93e9058c63",
)

maven_jar(
    name = "org_bitbucket_b_c_jose4j",
    artifact = "org.bitbucket.b_c:jose4j:0.6.5",
    sha1 = "524470e6ad000e3938f4c0f5e08bd423e95bd43a",
)

maven_jar(
    name = "net_jadler_jadler_all",
    artifact = "net.jadler:jadler-all:1.3.0",
    sha1 = "99b6b1ec94fc9671bfe97c1d47a740600f095d33",
)

maven_jar(
    name = "com_google_crypto_tink",
    artifact = "com.google.crypto.tink:tink:1.2.2",
    sha1 = "db27fd32f842b436ad07aecbe2934524473dc0ac",
)

maven_jar(
    name = "javax_servlet_javax_servlet_api",
    artifact = "javax.servlet:javax.servlet-api:4.0.1",
    sha1 = "a27082684a2ff0bf397666c3943496c44541d1ca",
)

maven_jar(
    name = "org_slf4j_slf4j_simple",
    artifact = "org.slf4j:slf4j-simple:1.7.27",
    sha1 = "59661ca47034c6a72e255e5803ae4019d2bd55c9",
)

### === END === Java Spark dependencies

# GRPC/Protobuf rules
http_archive(
    name = "build_stack_rules_proto",
    sha256 = "8a9cf001e3ba5c97d45ed8eb09985f15355df4bbe2dc6dd4844cccfe71f17d3e",
    strip_prefix = "rules_proto-9e68c7eb1e36bd08e9afebc094883ebc4debdb09",
    urls = ["https://github.com/stackb/rules_proto/archive/9e68c7eb1e36bd08e9afebc094883ebc4debdb09.tar.gz"],
)

# Newer version than what's loaded by @io_grpc_grpc_java grpc_java_repositories()
http_archive(
    name = "com_google_protobuf_javalite",
    sha256 = "a8cb9b8db16aff743a4bc8193abec96cf6ac0b0bc027121366b43ae8870f6fd3",
    strip_prefix = "protobuf-fa08222434bc58d743e8c2cc716bc219c3d0f44e",
    urls = ["https://github.com/google/protobuf/archive/fa08222434bc58d743e8c2cc716bc219c3d0f44e.zip"],
)

# Used by java_grpc_library

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "b1dcce395bdb6c620d3142597b5017f7175c527b0f9ae46c456726940876347e",
    strip_prefix = "grpc-java-%s" % GRPC_JAVA_VERSION,
    urls = [
        "https://github.com/grpc/grpc-java/archive/v%s.zip" % GRPC_JAVA_VERSION,
    ],
)

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories(omit_com_google_protobuf = True)

RULES_JVM_EXTERNAL_TAG = "2.9"

RULES_JVM_EXTERNAL_SHA = "e5b97a31a3e8feed91636f42e19b11c49487b85e5de2f387c999ea14d77c7f45"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    name = "java_uuid_generator",
    artifacts = [
        "com.fasterxml.uuid:java-uuid-generator:3.1.5",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party:java_uuid_generator_install.json",
    repositories = [
        "https://repo.maven.apache.org/maven2/",
    ],
)
load("@java_uuid_generator//:defs.bzl", java_uuid_generator_pin = "pinned_maven_install")

java_uuid_generator_pin()

maven_install(
    name = "io_reactivex_rxjava3_rxjava",
    artifacts = [
        "io.reactivex.rxjava3:rxjava:3.0.0-RC4",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party:io_reactivex_rxjava3_rxjava_install.json",
    repositories = [
        "https://repo.maven.apache.org/maven2/",
    ],
)

load("@io_reactivex_rxjava3_rxjava//:defs.bzl", io_reactivex_rxjava3_rxjava_pin = "pinned_maven_install")

io_reactivex_rxjava3_rxjava_pin()

maven_install(
    name = "io_token",
    artifacts = [
        "io.token.sdk:tokenio-sdk-core:2.6.4",
        "io.token.sdk:tokenio-sdk-tpp:2.6.4",
        "io.token.proto:common:1.1.103",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party:io_token_install.json",
    repositories = [
        # For direct deps
        "https://token.jfrog.io/token/public-libs-release-local/",
        # For transitive deps
        "https://repo1.maven.org/maven2",
    ],
)

load("@io_token//:defs.bzl", io_token_pin = "pinned_maven_install")
io_token_pin()

# Use via //third_party/jetty_server9
maven_install(
    name = "jetty_server9",
    artifacts = [
        "org.eclipse.jetty:jetty-util:9.4.15.v20190215",
        "org.eclipse.jetty:jetty-server:9.4.15.v20190215",
        "org.eclipse.jetty:jetty-http:9.4.15.v20190215",
        "javax.servlet:javax.servlet-api:3.1.0",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party:jetty_server9_install.json",
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
)

load("@jetty_server9//:defs.bzl", pin_jetty_server9 = "pinned_maven_install")

pin_jetty_server9()

maven_install(
    name = "lombok",
    artifacts = [
        "org.projectlombok:lombok:1.18.10",
    ],
    fetch_sources = False,
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
)

maven_install(
    name = "standalone_agent_deps",
    artifacts = [
        "org.springframework:spring-aop:5.2.1.RELEASE",
        "org.springframework:spring-context:5.2.1.RELEASE",
        "org.springframework:spring-core:5.2.1.RELEASE",
        "org.springframework:spring-expression:5.2.1.RELEASE",
        "org.springframework:spring-beans:5.2.1.RELEASE",
        "org.springframework:spring-web:5.2.1.RELEASE",
        "org.springframework:spring-test:5.2.1.RELEASE",
        "commons-codec:commons-codec:1.11",
        "commons-logging:commons-logging:1.2",
        "org.apache.httpcomponents:httpclient:4.5.10",
        "org.apache.httpcomponents:httpcore:4.4.12",
        "org.slf4j:jcl-over-slf4j:1.7.29",
        "org.slf4j:slf4j-api:1.7.29",
        "org.slf4j:jul-to-slf4j:1.7.29",
        "org.slf4j:jcl-over-slf4j:1.7.29",
        "org.slf4j:log4j-over-slf4j:1.7.29",
        "ch.qos.logback:logback-core:1.2.3",
        "ch.qos.logback:logback-classic:1.2.3",
    ],
    fetch_sources = False,
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
)

maven_install(
    name = "standalone_agent_test_deps",
    artifacts = [
        "org.springframework:spring-test:5.2.1.RELEASE",
    ],
    fetch_sources = False,
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
)

maven_install(
    name = "selenium",
    artifacts = [
        "com.codeborne:phantomjsdriver:1.4.3",
        "org.seleniumhq.selenium:selenium-support:3.4.0",
        "org.seleniumhq.selenium:selenium-android-driver:2.39.0",
        "org.seleniumhq.selenium:selenium-api:3.4.0",
        "org.seleniumhq.selenium:selenium-chrome-driver:3.4.0",
        "org.seleniumhq.selenium:selenium-firefox-driver:3.4.0",
        "org.seleniumhq.selenium:selenium-htmlunit-driver:2.52.0",
        "org.seleniumhq.selenium:selenium-ie-driver:3.4.0",
        "org.seleniumhq.selenium:selenium-iphone-driver:2.39.0",
        "org.seleniumhq.selenium:selenium-java:3.4.0",
        "org.seleniumhq.selenium:selenium-remote-driver:3.4.0",
        "org.seleniumhq.selenium:selenium-safari-driver:3.4.0",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party:selenium_install.json",
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
)

load("@selenium//:defs.bzl", pin_selenium = "pinned_maven_install")

pin_selenium()

maven_install(
    name = "com_salesforce_servicelibs_grpc_testing_contrib",
    artifacts = [
        "com.salesforce.servicelibs:grpc-testing-contrib:0.8.1",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party:com_salesforce_servicelibs_grpc_testing_contrib_install.json",
    repositories = [
        "https://repo.maven.apache.org/maven2/",
    ],
    excluded_artifacts = [
        "io.netty:*",
    ]
)
load("@com_salesforce_servicelibs_grpc_testing_contrib//:defs.bzl", com_salesforce_servicelibs_grpc_testing_contrib_pin = "pinned_maven_install")
com_salesforce_servicelibs_grpc_testing_contrib_pin()
