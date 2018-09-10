## Tink virtual monorepsotiroy
# These are repositories under Tink control. They are trusted, and imported
# as a part of tink-backend's repository (not checksumed).

# Use the new Skylark version of git_repository. This uses the system's native
# git client which supports fancy key formats and key passphrases.
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

git_repository(
    name = "dropwizard_jersey",
    commit = "0c2f90f4358e262d0fe0af3f6d31eb0fa3cabc40",
    remote = "git@github.com:tink-ab/dropwizard.git",
)

# Docker dependencies
git_repository(
    name = "io_bazel_rules_docker",
    remote = "https://github.com/bazelbuild/rules_docker.git",
    commit = "9dd92c73e7c8cf07ad5e0dca89a3c3c422a3ab7d" # v0.3.0"
)

load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_pull",
    container_repositories = "repositories",
)

git_repository(
    name = "se_tink_httpcore",
    remote = "git@github.com:tink-ab/httpcomponents-core.git",
    commit = "a20a8723692a66dd5b57dd002013811e02f2d127"
)

git_repository(
    name = "se_tink_httpclient",
    remote = "git@github.com:tink-ab/httpcomponents-client.git",
    commit = "037f32c031ebfd13a60e84635786532b3cb11454"
)

container_repositories()

container_pull(
    name = "openjdk-jdk8",
    registry = "index.docker.io",
    repository = "library/openjdk",
    # TODO (carl.loa.odin@tink.se): Change to use digest for reproducability
    # KeyError: 'layers'` error when using docker_pull with a digest
    # https://github.com/bazelbuild/rules_docker/issues/167
    tag = "8u151-jdk-slim",
)

## External dependencies
# External repositories that are not under Tink control. These should *always*
# be locked to something stable (a commit, not a branch for example) and have
# a checksum to prevent tampering by the remote end.

# Use pubref's implementation of protobuf rules for gRPC support
git_repository(
    name = "org_pubref_rules_protobuf",
    remote = "git@github.com:pubref/rules_protobuf",
    commit = "563b674a2ce6650d459732932ea2bc98c9c9a9bf",
)

# TODO: Build these
http_file(
    name = "protoc_gen_grpc_java_linux_x86_64",
    urls = ["http://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.2.0/protoc-gen-grpc-java-1.2.0-linux-x86_64.exe"],
    sha256 = "6f5fc69224f2fa9ed7e1376aedf6c5c6239dcfe566beb89d3a1c77c50fb8886b",
)

http_file(
    name = "protoc_gen_grpc_java_macosx",
    urls = ["http://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.2.0/protoc-gen-grpc-java-1.2.0-osx-x86_64.exe"],
    sha256 = "f7ad13d42e2a2415d021263ae258ca08157e584c54e9fce093f1a5a871a8763a",
)

# proto_library rules implicitly depend on @com_google_protobuf//:protoc,
# which is the proto-compiler.
http_archive(
    name = "com_google_protobuf",
    sha256 = "cef7f1b5a7c5fba672bec2a319246e8feba471f04dcebfe362d55930ee7c1c30",
    strip_prefix = "protobuf-3.5.0",
    urls = ["https://github.com/google/protobuf/archive/v3.5.0.zip"],
)

bind(
    name = "protocol_compiler",
    actual = "@com_google_protobuf//:protoc",
)

maven_jar(
    name = "com_google_api_grpc_proto_google_common_protos",
    artifact = "com.google.api.grpc:proto-google-common-protos:0.1.9",
    sha1 = "3760f6a6e13c8ab070aa629876cdd183614ee877",
)

maven_jar(
    name = "io_opencensus_opencensus_api",
    artifact = "io.opencensus:opencensus-api:0.12.3",
    sha1 = "743f074095f29aa985517299545e72cc99c87de0",
)

maven_jar(
    name = "io_opencensus_opencensus_grpc_metrics",
    artifact = "io.opencensus:opencensus-contrib-grpc-metrics:0.12.3",
    sha1 = "a4c7ff238a91b901c8b459889b6d0d7a9d889b4d",
)


maven_jar(
    name = "com_google_errorprone_error_prone_annotations",
    artifact = "com.google.errorprone:error_prone_annotations:2.0.11",
    sha1 = "3624d81fca4e93c67f43bafc222b06e1b1e3b260",
)

# CSS SASS
http_archive(
    name = "io_bazel_rules_sass",
    urls = ["https://github.com/bazelbuild/rules_sass/archive/0.0.3.zip"],
    sha256 = "8fa98e7b48a5837c286a1ea254b5a5c592fced819ee9fe4fdd759768d97be868",
    strip_prefix = "rules_sass-0.0.3",
)

load("@io_bazel_rules_sass//sass:sass.bzl", "sass_repositories")

# This imports the SASS compiler using the io_bazel_rules_sass build rules.
# It is using sha256 checksums, so it's fine - but if upgrading make sure to
# make sure that the new version is doing the same.
sass_repositories()

## Maven jar imports
# Same as above, always make sure to specify a checksum. To check what is using
# a specifc library you can use:
# $ bazel query 'rdeps(//:all, @aopalliance_aopalliance//jar, 1)'
# //third_party:org_springframework_data_spring_cql
# //third_party:com_google_inject_guice
# @aopalliance_aopalliance//jar:jar

maven_jar(
    name = "org_springframework_data_spring_cql",
    artifact = "org.springframework.data:spring-cql:1.5.1.RELEASE",
    sha1 = "15de7e2c2e1c600afd338e5547aaacf11d3c2667",
)

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
    name = "javax_annotation_jsr250_api",
    artifact = "javax.annotation:jsr250-api:1.0",
    sha1 = "5025422767732a1ab45d93abfea846513d742dcf",
)

maven_jar(
    name = "org_eclipse_jetty_toolchain_setuid_jetty_setuid_java",
    artifact = "org.eclipse.jetty.toolchain.setuid:jetty-setuid-java:1.0.2",
    sha1 = "4dc7fca46ac6badff4336c574b2c713ac3e40f73",
)

maven_jar(
    name = "com_maxmind_geoip2_geoip2",
    artifact = "com.maxmind.geoip2:geoip2:0.7.0",
    sha1 = "6997435a72ae4f6e4bc298ac5b1ae67e18715bc0",
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
    name = "org_seleniumhq_selenium_selenium_firefox_driver",
    artifact = "org.seleniumhq.selenium:selenium-firefox-driver:2.29.0",
    sha1 = "135043df1b5af4b672de5fe9255e8dfb7382204d",
)

maven_jar(
    name = "com_codahale_metrics_metrics_httpclient",
    artifact = "com.codahale.metrics:metrics-httpclient:3.0.2",
    sha1 = "c658daf41b1ecf934ccd21e83eeeb18703355afb",
)

maven_jar(
    name = "org_freemarker",
    artifact = "org.freemarker:freemarker:2.3.27-incubating",
    sha1 = "fa71f754fd3c4123358c2b1e5eef630cd99d752f"
)

maven_jar(
    name = "org_owasp_esapi_esapi",
    artifact = "org.owasp.esapi:esapi:2.0.1",
    sha1 = "2ea3b87c948dbc0c77a17fe24fda961ecc38c6f2",
)

maven_jar(
    name = "com_google_protobuf_protobuf_java_util",
    artifact = "com.google.protobuf:protobuf-java-util:3.5.1",
    sha1 = "6e40a6a3f52455bd633aa2a0dba1a416e62b4575",
)

maven_jar(
    name = "io_netty_netty_resolver",
    artifact = "io.netty:netty-resolver:4.1.22.Final",
    sha1 = "b5484d17a97cb57b07d2a1ac092c249e47234c17",
)

maven_jar(
    name = "io_netty_netty_resolver_dns",
    artifact = "io.netty:netty-resolver-dns:4.1.22.Final",
    sha1 = "fc1e5682abfd5ad856e671e549ca10da41f32163",
)
maven_jar(
    name = "io_netty_netty_codec_dns",
    artifact = "io.netty:netty-codec-dns:4.1.22.Final",
    sha1 = "0cd8d3aa9414edb6083627657d48bdc7cec2b8f5",
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
    name = "net_sf_qualitycheck_quality_check",
    artifact = "net.sf.qualitycheck:quality-check:1.3",
    sha1 = "5387135c674a783fd2dec7f05b6c17a7f6517c8b",
)

maven_jar(
    name = "com_googlecode_libphonenumber_libphonenumber",
    artifact = "com.googlecode.libphonenumber:libphonenumber:5.7",
    sha1 = "20140c130456845cc73f3b2a4bf50c7fe3a37b77",
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
    artifact = "net.java.dev.jna:jna-platform:4.5.1",
    sha1 = "117d52c9f672d8b7ea80a81464c33ef843de9254",
)

maven_jar(
    name = "com_google_inject_extensions_guice_multibindings",
    artifact = "com.google.inject.extensions:guice-multibindings:4.1.0",
    sha1 = "3b27257997ac51b0f8d19676f1ea170427e86d51",
)

maven_jar(
    name = "com_fasterxml_jackson_core_jackson_databind",
    artifact = "com.fasterxml.jackson.core:jackson-databind:2.8.8.1",
    sha1 = "fb1930cd0b1b36924717229f56ca80d785401ff8",
)

maven_jar(
    name = "xmlpull_xmlpull",
    artifact = "xmlpull:xmlpull:1.1.3.1",
    sha1 = "2b8e230d2ab644e4ecaa94db7cdedbc40c805dfa",
)

maven_jar(
    name = "com_fasterxml_jackson_core_jackson_core",
    artifact = "com.fasterxml.jackson.core:jackson-core:2.8.8",
    sha1 = "d478fb6de45a7c3d2cad07c8ad70c7f0a797a020",
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
    name = "com_google_protobuf_protobuf_java",
    artifact = "com.google.protobuf:protobuf-java:3.5.1",
    sha1 = "8c3492f7662fa1cbf8ca76a0f5eb1146f7725acd",
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
    artifact = "com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.8.8",
    sha1 = "e2e95efc46d45be4b429b704efbb1d4b89721d3a",
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

maven_jar(
    name = "io_netty_netty_codec",
    artifact = "io.netty:netty-codec:4.1.22.Final",
    sha1 = "239c0af275952e70bb4adf7cf8c03d88ddc394c9",
)

maven_jar(
    name = "org_seleniumhq_selenium_selenium_ie_driver",
    artifact = "org.seleniumhq.selenium:selenium-ie-driver:2.29.0",
    sha1 = "5761f8a5a5cba5db62cb9ac082cdcdab5509a7d5",
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
    name = "org_joda_joda_convert",
    artifact = "org.joda:joda-convert:1.2",
    sha1 = "35ec554f0cd00c956cc69051514d9488b1374dec",
)

maven_jar(
    name = "com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
    artifact = "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.8.8",
    sha1 = "e70be1cd2ad47615a527f7475f63acadbf3be730",
)

maven_jar(
    name = "org_slf4j_slf4j_api",
    artifact = "org.slf4j:slf4j-api:1.7.21",
    sha1 = "139535a69a4239db087de9bab0bee568bf8e0b70",
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
    name = "com_github_detro_ghostdriver_phantomjsdriver",
    artifact = "com.github.detro.ghostdriver:phantomjsdriver:1.0.3",
    sha1 = "17fdf2fe6bb281a7d37fdcb0fd87c77a78bf0d76",
)

maven_jar(
    name = "com_fasterxml_jackson_core_jackson_annotations",
    artifact = "com.fasterxml.jackson.core:jackson-annotations:2.8.8",
    sha1 = "1ed81c0e4eb2d261d1da0a3a45bd6b199fb5cf9a",
)

maven_jar(
    name = "net_sf_uadetector_uadetector_resources",
    artifact = "net.sf.uadetector:uadetector-resources:2014.10",
    sha1 = "973d8ec015a740b20cd3cdbc593315c521cb0128",
)

maven_jar(
    name = "org_apache_httpcomponents_httpclient",
    artifact = "org.apache.httpcomponents:httpclient:4.5.5",
    sha1 = "1603dfd56ebcd583ccdf337b6c3984ac55d89e58",
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
    name = "javax_servlet_javax_servlet_api",
    artifact = "javax.servlet:javax.servlet-api:3.0.1",
    sha1 = "6bf0ebb7efd993e222fc1112377b5e92a13b38dd",
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
    name = "javax_mail_mail",
    artifact = "javax.mail:mail:1.4.5",
    sha1 = "85319c87280f30e1afc54c355f91f44741beac49",
)

maven_jar(
    name = "c3p0_c3p0",
    artifact = "c3p0:c3p0:0.9.1.1",
    sha1 = "302704f30c6e7abb7a0457f7771739e03c973e80",
)

maven_jar(
    name = "io_netty_netty_handler_proxy",
    artifact = "io.netty:netty-handler-proxy:4.1.22.Final",
    sha1 = "8eabe24f0b8e95d0873964666ad070179ca81e72",
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
    name = "net_sf_opencsv_opencsv",
    artifact = "net.sf.opencsv:opencsv:2.0",
    sha1 = "97a2765dc2db1083e7c5afcc210db5f7cad3b442",
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
    artifact = "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.8.8",
    sha1 = "634c0ae9a100cd381bc1682796452418964e1408",
)

maven_jar(
    name = "net_java_dev_jna_jna",
    artifact = "net.java.dev.jna:jna:4.5.1",
    sha1 = "65bd0cacc9c79a21c6ed8e9f588577cd3c2f85b9",
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
    name = "de_grundid_opendatalab_geojson_jackson",
    artifact = "de.grundid.opendatalab:geojson-jackson:1.6",
    sha1 = "15535b9779fef7395c98533eb4b5c8b1e0479023",
)

maven_jar(
    name = "org_apache_geronimo_specs_geronimo_jpa_2_0_spec",
    artifact = "org.apache.geronimo.specs:geronimo-jpa_2.0_spec:1.1",
    sha1 = "f4d90788691f5f5f201f39a53a23d392cde660a3",
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
    name = "com_restfb_restfb",
    artifact = "com.restfb:restfb:1.43.0",
    sha1 = "8689d1e66a4800f5945c4d4874996d22f4fe9a25",
)

maven_jar(
    name = "org_apache_commons_commons_collections4",
    artifact = "org.apache.commons:commons-collections4:4.0",
    sha1 = "da217367fd25e88df52ba79e47658d4cf928b0d1",
)

maven_jar(
    name = "org_scala_lang_scala_library",
    artifact = "org.scala-lang:scala-library:2.10.5",
    sha1 = "57ac67a6cf6fd591e235c62f8893438e8d10431d",
)

maven_jar(
    name = "io_intercom_intercom_java",
    artifact = "io.intercom:intercom-java:2.2.3",
    repository = "http://jcenter.bintray.com",
    sha1 = "7786e9027bb186313fee886463ddeb6b895f1794",
)

maven_jar(
    name = "org_opensaml_openws",
    artifact = "org.opensaml:openws:1.5.0",
    sha1 = "4cb7919a5cbb9203a56c63487cc9f773dfa9930f",
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
    artifact = "com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.8.8",
    sha1 = "d0e5d68dff7c0ee09a4b0fb977b27d999acd54e5",
)

maven_jar(
    name = "io_dropwizard_dropwizard_metrics",
    artifact = "io.dropwizard:dropwizard-metrics:0.7.1",
    sha1 = "6ca1d7d1d1d1bcf7c803a127e0c6696d1c98fdb1",
)

maven_jar(
    name = "org_apache_thrift_libthrift",
    artifact = "org.apache.thrift:libthrift:0.9.2",
    sha1 = "9b067e2e2c5291e9f0d8b3561b1654286e6d81ee",
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
    name = "com_jcraft_jsch",
    artifact = "com.jcraft:jsch:0.1.53",
    sha1 = "658b682d5c817b27ae795637dfec047c63d29935",
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
    name = "org_antlr_antlr4_runtime",
    artifact = "org.antlr:antlr4-runtime:4.0",
    sha1 = "02ddf21287c175a7f1d348745f3fbf43730faba3",
)

maven_jar(
    name = "org_json_json",
    artifact = "org.json:json:20080701",
    sha1 = "d652f102185530c93b66158b1859f35d45687258",
)

maven_jar(
    name = "com_googlecode_concurrent_trees_concurrent_trees",
    artifact = "com.googlecode.concurrent-trees:concurrent-trees:1.0.0",
    sha1 = "af805a5c473736f81277b6211bfc9f853c8566a7",
)

maven_jar(
    name = "commons_pool_commons_pool",
    artifact = "commons-pool:commons-pool:1.5.4",
    sha1 = "75b6e20c596ed2945a259cea26d7fadd298398e6",
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
    name = "org_seleniumhq_selenium_selenium_chrome_driver",
    artifact = "org.seleniumhq.selenium:selenium-chrome-driver:2.29.0",
    sha1 = "199a8fdce2717f1929c17d4929c35beea5301511",
)

maven_jar(
    name = "com_googlecode_gettext_commons_gettext_commons",
    artifact = "com.googlecode.gettext-commons:gettext-commons:0.9.8",
    sha1 = "20e498b37fcced2f3fa273df2fae169e6b4e8061",
)

maven_jar(
    name = "com_mandrillapp_wrapper_lutung_lutung",
    artifact = "com.mandrillapp.wrapper.lutung:lutung:0.0.3",
    sha1 = "5d8854c1c5b3af84ab7c8f14004cd201db7b8168",
)

maven_jar(
    name = "mysql_mysql_connector_java",
    artifact = "mysql:mysql-connector-java:5.1.42",
    sha1 = "80a448a3ec2178b649bb2e3cb3610fab06e11669",
)

maven_jar(
    name = "com_fasterxml_jackson_datatype_jackson_datatype_joda",
    artifact = "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.8.8",
    sha1 = "778f71765e683037bc8f6440d6afaceb91085852",
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
    name = "commons_beanutils_commons_beanutils",
    artifact = "commons-beanutils:commons-beanutils:1.7.0",
    sha1 = "5675fd96b29656504b86029551973d60fb41339b",
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
    artifact = "commons-codec:commons-codec:1.10",
    sha1 = "4b95f4897fa13f2cd904aee711aeafc0c5295cd8",
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
    name = "io_dropwizard_dropwizard_metrics_graphite",
    artifact = "io.dropwizard:dropwizard-metrics-graphite:0.7.1",
    sha1 = "fa2d12fb3f9367d96ba6282a9081a4444bc1d426",
)

maven_jar(
    name = "org_bouncycastle_bcprov_jdk15",
    artifact = "org.bouncycastle:bcprov-jdk15:1.46",
    sha1 = "d726ceb2dcc711ef066cc639c12d856128ea1ef1",
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
    name = "org_apache_maven_maven_artifact",
    artifact = "org.apache.maven:maven-artifact:3.2.3",
    sha1 = "e75bf0b42e46bada3c1883fec3f7ef57cce84161",
)

maven_jar(
    name = "org_apache_velocity_velocity",
    artifact = "org.apache.velocity:velocity:1.7",
    sha1 = "2ceb567b8f3f21118ecdec129fe1271dbc09aa7a",
)

maven_jar(
    name = "com_fasterxml_jackson_module_jackson_module_afterburner",
    artifact = "com.fasterxml.jackson.module:jackson-module-afterburner:2.8.8",
    sha1 = "4cdd631e6876be5b8344ceb1649f585961901fc8",
)

maven_jar(
    name = "io_netty_netty_common",
    artifact = "io.netty:netty-common:4.1.22.Final",
    sha1 = "56ff4deca53fc791ed59ac2b72eb6718714a4de9",
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

maven_jar(
    name = "io_netty_netty_handler",
    artifact = "io.netty:netty-handler:4.1.22.Final",
    sha1 = "a3a16b17d5a5ed6f784b0daba95e28d940356109",
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
    artifact = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.8.8",
    sha1 = "fe81db224d5f06539de17aceb30ea7ba6ef8a300",
)

maven_jar(
    name = "org_springframework_security_core",
    artifact = "org.springframework.security:spring-security-core:4.2.3.RELEASE",
    sha1 = "5c0e47a47036c94d6fdd02696bf52be6d1adbd4d"
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
    artifact = "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.8.8",
    sha1 = "3f5e135fd48af6c7fb432166c5b658e9133114ea",
)

maven_jar(
    name = "org_apache_curator_curator_recipes",
    artifact = "org.apache.curator:curator-recipes:4.0.0",
    sha1 = "46ca001305a74a277d8a42f377bb7c901c0423bf",
)

maven_jar(
    name = "org_apache_httpcomponents_httpcore",
    artifact = "org.apache.httpcomponents:httpcore:4.4.9",
    sha1 = "a86ce739e5a7175b4b234c290a00a5fdb80957a0",
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
    name = "net_sourceforge_jexcelapi_jxl",
    artifact = "net.sourceforge.jexcelapi:jxl:2.6.12",
    sha1 = "7faf62e0697f7a88954622dfe8c8de33ed142ac7",
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

maven_jar(
    name = "io_netty_netty_codec_socks",
    artifact = "io.netty:netty-codec-socks:4.1.22.Final",
    sha1 = "d077b39da2dedc5dc5db50a44e5f4c30353e86f3",
)

maven_jar(
    name = "io_netty_netty_buffer",
    artifact = "io.netty:netty-buffer:4.1.22.Final",
    sha1 = "15e964a2095031364f534a6e21977f5ee9ca32a9",
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
    name = "net_sf_uadetector_uadetector_core",
    artifact = "net.sf.uadetector:uadetector-core:0.9.22",
    sha1 = "e40e4e391e27fdb798a574a3ee0c26debe36c6cd",
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
    name = "com_amazonaws_aws_java_sdk",
    artifact = "com.amazonaws:aws-java-sdk:1.4.4.2",
    sha1 = "aed880d0fc444c0d147c58dc337ec94204042b58",
)

maven_jar(
    name = "com_amazonaws_jmespath_java",
    artifact = "com.amazonaws:jmespath-java:1.11.381",
    sha1 = "c428e44fa35032bbc89c6aaab15f1f3857c2afbe"
    )

maven_jar(
    name = "com_amazonaws_aws_java_sdk_core",
    artifact = "com.amazonaws:aws-java-sdk-core:1.11.381",
    sha1 = "7540dfa848acd7770e21ef982d67fbb612b33d7f"
    )

maven_jar(
    name = "com_amazonaws_aws_java_sdk_code_generator",
    artifact = "com.amazonaws:aws-java-sdk-code-generator:1.11.381",
    sha1 = "d8ed76e95c0313e3b9898c7cc5cc40b935d0c32b"
    )

maven_jar(
    name = "com_amazonaws_aws_java_sdk_sqs",
    artifact = "com.amazonaws:aws-java-sdk-sqs:1.11.381",
    sha1 = "4ea1ad090c04fde806eb4fa24b10fa5748900ef1"
    )

maven_jar(
    name = "org_springframework_data_spring_data_jpa",
    artifact = "org.springframework.data:spring-data-jpa:1.11.1.RELEASE",
    sha1 = "fa362aecd78883991f57a5d64e19f34b57a2c34d",
)

maven_jar(
    name = "org_liquibase_liquibase_core",
    artifact = "org.liquibase:liquibase-core:3.1.1",
    sha1 = "8f69599b04156a222ae7cd00b0dd844f1323b683",
)

maven_jar(
    name = "io_netty_netty_codec_http2",
    artifact = "io.netty:netty-codec-http2:4.1.22.Final",
    sha1 = "6d01daf652551a3219cc07122b765d4c4924dcf8",
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
    name = "com_turo_pushy",
    artifact = "com.turo:pushy:0.12.0",
    sha1 = "24c55011bb2da52a79edbbd21933766ae1047a5f",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_http",
    artifact = "org.eclipse.jetty:jetty-http:9.0.7.v20131107",
    sha1 = "67060a59b426c76a2788ea5f4e19c1d3170ac562",
)

maven_jar(
    name = "org_seleniumhq_selenium_selenium_api",
    artifact = "org.seleniumhq.selenium:selenium-api:2.29.0",
    sha1 = "4292cc882785ccf771ddbfcd2f0e04a6c7a1155f",
)

maven_jar(
    name = "org_seleniumhq_selenium_selenium_iphone_driver",
    artifact = "org.seleniumhq.selenium:selenium-iphone-driver:2.29.0",
    sha1 = "c08b55569e5947e7a60cd15c5029761230081f7e",
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

maven_jar(
    name = "io_netty_netty_transport",
    artifact = "io.netty:netty-transport:4.1.22.Final",
    sha1 = "3bd455cd9e5e5fb2e08fd9cd0acfa54c079ca989",
)

maven_jar(
    name = "org_scala_lang_scala_reflect",
    artifact = "org.scala-lang:scala-reflect:2.10.5",
    sha1 = "7392facb48876c67a89fcb086112b195f5f6bbc3",
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
    name = "org_quartz_scheduler_quartz",
    artifact = "org.quartz-scheduler:quartz:2.2.3",
    sha1 = "d4d8ea088852beeb89f54d3040fe1cbaa8491dcd",
)

maven_jar(
    name = "apporiented_com_hierarchical_clustering",
    artifact = "com.apporiented:hierarchical-clustering:1.0.3",
    sha1 = "802582e69569b283cd91010d640cd4f851759a88",
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
    name = "org_seleniumhq_selenium_selenium_htmlunit_driver",
    artifact = "org.seleniumhq.selenium:selenium-htmlunit-driver:2.29.0",
    sha1 = "387b28342ff5fcc814c3e461986191427cd115c6",
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
    name = "ca_juliusdavies_not_yet_commons_ssl",
    artifact = "ca.juliusdavies:not-yet-commons-ssl:0.3.9",
    sha1 = "e20f0960c000681c91d00de846a43cf2051b8f69",
)

maven_jar(
    name = "org_bouncycastle_bcprov_jdk15on",
    artifact = "org.bouncycastle:bcprov-jdk15on:1.59",
    sha1 = "2507204241ab450456bdb8e8c0a8f986e418bd99",
)

maven_jar(
    name = "org_seleniumhq_selenium_selenium_remote_driver",
    artifact = "org.seleniumhq.selenium:selenium-remote-driver:2.29.0",
    sha1 = "8651b5f954e15f39d17a26557d27e99b960c401e",
)

maven_jar(
    name = "org_seleniumhq_selenium_selenium_safari_driver",
    artifact = "org.seleniumhq.selenium:selenium-safari-driver:2.29.0",
    sha1 = "79c53be06ba4739c969a53c7c2f702c4d3a439bc",
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

maven_jar(
    name = "io_netty_netty_tcnative_boringssl_static",
    artifact = "io.netty:netty-tcnative-boringssl-static:2.0.7.Final",
    sha1 = "a8ec0f0ee612fa89c709bdd3881c3f79fa00431d",
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
    name = "org_jvnet_jaxb2_commons_jaxb2_basics_runtime",
    artifact = "org.jvnet.jaxb2_commons:jaxb2-basics-runtime:0.6.5",
    sha1 = "d6142ae0b68f06dbab141eb0533659f90e05bcde",
)

maven_jar(
    name = "com_github_jknack_handlebars",
    artifact = "com.github.jknack:handlebars:0.10.0",
    sha1 = "18269e1c33c61d410b88a622083aaca940f9e493",
)

maven_jar(
    name = "org_jfree_jfreesvg",
    artifact = "org.jfree:jfreesvg:2.1",
    sha1 = "206679ef09f39b57b2f73dc79bd09785efe007af",
)

maven_jar(
    name = "io_prometheus_simpleclient_logback",
    artifact = "io.prometheus:simpleclient_logback:0.0.19",
    sha1 = "17623f01b8caa5300ec5a3d16cced2f12c0ac8fb",
)

maven_jar(
    name = "com_github_rholder_guava_retrying",
    artifact = "com.github.rholder:guava-retrying:2.0.0",
    sha1 = "974bc0a04a11cc4806f7c20a34703bd23c34e7f4",
)

maven_jar(
    name = "org_opensaml_xmltooling",
    artifact = "org.opensaml:xmltooling:1.4.0",
    sha1 = "841308b1a32a0bdaaad3d469a7133eea25103c7b",
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
    name = "io_reactivex_rxjava",
    artifact = "io.reactivex:rxjava:1.0.13",
    sha1 = "b8668706414b5936d38c8a9e4c2be16e3c999c62",
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

maven_jar(
    name = "io_netty_netty_codec_http",
    artifact = "io.netty:netty-codec-http:4.1.22.Final",
    sha1 = "3805f3ca0d57630200defc7f9bb6ed3382dcb10b",
)

maven_jar(
    name = "org_apache_santuario_xmlsec",
    artifact = "org.apache.santuario:xmlsec:1.5.4",
    sha1 = "dff1ca1279fd24182ee530982927156256c02f8d",
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
    artifact = "com.fasterxml.jackson.jaxrs:jackson-jaxrs-base:2.8.8",
    sha1 = "345a87f3c145912163964ded45803ebf0a9c775e",
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
    name = "org_opensaml_opensaml",
    artifact = "org.opensaml:opensaml:2.6.0",
    sha1 = "9ac35d10b55430a015493c1a4ebdc8ece6f67e6f",
)

maven_jar(
    name = "com_google_guava_guava",
    artifact = "com.google.guava:guava:21.0",
    sha1 = "3a3d111be1be1b745edfa7d91678a12d7ed38709",
)

maven_jar(
    name = "com_codahale_metrics_metrics_graphite",
    artifact = "com.codahale.metrics:metrics-graphite:3.0.2",
    sha1 = "88626d5a305d1c018ac777162ea483f934cf196b",
)

maven_jar(
    name = "org_codehaus_jackson_jackson_mapper_asl",
    artifact = "org.codehaus.jackson:jackson-mapper-asl:1.8.9",
    sha1 = "e430ed24d67dfc126ee16bc23156a044950c3168",
)

maven_jar(
    name = "org_seleniumhq_selenium_selenium_java",
    artifact = "org.seleniumhq.selenium:selenium-java:2.29.0",
    sha1 = "cb582e26966dcd641910b446ab559e14b71ceb07",
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
    name = "org_seleniumhq_selenium_selenium_android_driver",
    artifact = "org.seleniumhq.selenium:selenium-android-driver:2.29.0",
    sha1 = "e254c2f1266dbc7ecaad4508f797b48d8cecd299",
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
    name = "com_twitter_jsr166e",
    artifact = "com.twitter:jsr166e:1.1.0",
    sha1 = "233098147123ee5ddcd39ffc57ff648be4b7e5b2",
)

maven_jar(
    name = "com_codahale_metrics_metrics_json",
    artifact = "com.codahale.metrics:metrics-json:3.0.2",
    sha1 = "46198fe0284104519b60ff2ad49f71c98ba942f2",
)

maven_jar(
    name = "com_stripe_stripe_java",
    artifact = "com.stripe:stripe-java:1.18.0",
    sha1 = "3d4782ca0f44546679e72d5db94219506ebeef0c",
)

maven_jar(
    name = "org_apache_hadoop_hadoop_core",
    artifact = "org.apache.hadoop:hadoop-core:1.2.1",
    sha1 = "3e5874122a26a735162a380627210779b41bfd59",
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
    name = "org_mortbay_jetty_servlet_api",
    artifact = "org.mortbay.jetty:servlet-api:2.5-20081211",
    sha1 = "22bff70037e1e6fa7e6413149489552ee2064702",
)

maven_jar(
    name = "org_mortbay_jetty_jsp_2_1",
    artifact = "org.mortbay.jetty:jsp-2.1:6.1.14",
    sha1 = "3a1df1e8e0fa56e9a940abbd19bc6f397fce16b5",
)

maven_jar(
    name = "org_mortbay_jetty_jsp_api_2_1",
    artifact = "org.mortbay.jetty:jsp-api-2.1:6.1.14",
    sha1 = "756b3fac155e31bd0e85545dbf953495e0455c73",
)

maven_jar(
    name = "org_mortbay_jetty_jetty_util",
    artifact = "org.mortbay.jetty:jetty-util:6.1.26",
    sha1 = "e5642fe0399814e1687d55a3862aa5a3417226a9",
)

maven_jar(
    name = "org_mortbay_jetty_jetty",
    artifact = "org.mortbay.jetty:jetty:6.1.26",
    sha1 = "2f546e289fddd5b1fab1d4199fbb6e9ef43ee4b0",
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
    artifact = "commons-cli:commons-cli:1.2",
    sha1 = "2bf96b7aa8b611c177d329452af1dc933e14501c",
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
    repository = "https://oss.sonatype.org/content/groups/jetty/",
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
    name = "io_protostuff_protostuff_collectionschema",
    artifact = "io.protostuff:protostuff-collectionschema:1.5.1",
    sha1 = "0e47e085db363cec2cc9f27320ad17aacff11e35",
)

maven_jar(
    name = "org_rythmengine_rythm_engine",
    artifact = "org.rythmengine:rythm-engine:1.1.7",
    sha1 = "1a9133bb668026221a3146360a81978b56af2a67",
)

maven_jar(
    name = "net_bytebuddy_byte_buddy",
    artifact = "net.bytebuddy:byte-buddy:1.5.5",
    sha1 = "8557b6465cea17f3769678235e77d5cb076c1170",
)

maven_jar(
    name = "org_objenesis_objenesis",
    artifact = "org.objenesis:objenesis:2.4",
    sha1 = "2916b6c96b50c5b3ec4452ed99401db745aabb27",
)

maven_jar(
    name = "org_mvel_mvel2",
    artifact = "org.mvel:mvel2:2.2.8.Final",
    sha1 = "a0d38dc2422594621b71604f91255af1001114c0",
)

maven_jar(
    name = "com_stevesoft_pat_pat",
    artifact = "com.stevesoft.pat:pat:1.5.3",
    sha1 = "4b96b36954e8f86023dd64ff24fa211d8bb16a98",
)

maven_jar(
    name = "org_eclipse_jdt_core_compiler_ecj",
    artifact = "org.eclipse.jdt.core.compiler:ecj:4.6.1",
    sha1 = "5555607add54eb79c5a0729134e663fe2e6300c2",
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
    artifact = "io.grpc:grpc-protobuf:1.11.0",
    sha1 = "b87abe640170a6eaee11834098e6eec6b53b85b3",
)

maven_jar(
    name = "io_grpc_grpc_auth",
    artifact = "io.grpc:grpc-auth:jar:1.11.0",
    sha1 = "3dfdf60818519be4847a1a9109a9b162dcf42803",
)

maven_jar(
    name = "io_grpc_grpc_context",
    artifact = "io.grpc:grpc-context:jar:1.11.0",
    sha1 = "ce572ec046f6967d34fbac35eda20b4d0ed58c78",
)

maven_jar(
    name = "io_grpc_grpc_protobuf_lite",
    artifact = "io.grpc:grpc-protobuf-lite:1.11.0",
    sha1 = "f2263f20d2545c208bd5bdd6ca47c3a164df0671",
)

maven_jar(
    name = "io_grpc_grpc_protobuf_nano",
    artifact = "io.grpc:grpc-protobuf-nano:1.11.0",
    sha1 = "a105e447c7bec8fd36ec908256dc3e2794873f4c",
)

maven_jar(
    name = "io_grpc_grpc_stub",
    artifact = "io.grpc:grpc-stub:1.11.0",
    sha1 = "692522483a8fd32916125733b7030c8c4b722f84",
)

maven_jar(
    name = "io_grpc_grpc_core",
    artifact = "io.grpc:grpc-core:1.11.0",
    sha1 = "d0aa483ab2189b16f06f101f6e82dd601bb4a266",
)

maven_jar(
    name = "io_grpc_grpc_netty",
    artifact = "io.grpc:grpc-netty:1.11.0",
    sha1 = "fd1f99288a19c0d881f02ca0fb35d9527a8e0f79",
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
    artifact = "org.reflections:reflections:0.9.9-RC2",
    sha1 = "98049bff327d2c02bfe78ac5acba1b83bf061297",
)

maven_jar(
    name = "com_thoughtworks_paranamer_paranamer",
    artifact = "com.thoughtworks.paranamer:paranamer:2.7",
    sha1 = "3ed64c69e882a324a75e890024c32a28aff0ade8",
)

maven_jar(
    name = "org_apache_commons_commons_compress",
    artifact = "org.apache.commons:commons-compress:1.8.1",
    sha1 = "a698750c16740fd5b3871425f4cb3bbaa87f529d",
)

maven_jar(
    name = "org_apache_avro_avro",
    artifact = "org.apache.avro:avro:1.8.2",
    sha1 = "91e3146dfff4bd510181032c8276a3a0130c0697",
)

maven_jar(
    name = "org_tukaani_xz",
    artifact = "org.tukaani:xz:1.5",
    sha1 = "9c64274b7dbb65288237216e3fae7877fd3f2bee",
)

maven_jar(
    name = "net_sf_supercsv_super_csv",
    artifact = "net.sf.supercsv:super-csv:2.1.0",
    sha1 = "c6466dd0e28c034272b9f70a3f1896c03f1f2b27",
)

maven_jar(
    name = "com_github_jbellis_jamm",
    artifact = "com.github.jbellis:jamm:0.3.0",
    sha1 = "a08af6071e57d4eb5d13db780c7810f73b549f1a",
)

maven_jar(
    name = "com_boundary_high_scale_lib",
    artifact = "com.boundary:high-scale-lib:1.0.6",
    sha1 = "7b44147cb2729e1724d2d46d7b932c56b65087f0",
)

maven_jar(
    name = "com_yammer_metrics_metrics_core",
    artifact = "com.yammer.metrics:metrics-core:2.2.0",
    sha1 = "f82c035cfa786d3cbec362c38c22a5f5b1bc8724",
)

maven_jar(
    name = "org_antlr_antlr_runtime",
    artifact = "org.antlr:antlr-runtime:3.5.2",
    sha1 = "cd9cd41361c155f3af0f653009dcecb08d8b4afd",
)

maven_jar(
    name = "com_googlecode_concurrentlinkedhashmap_concurrentlinkedhashmap_lru",
    artifact = "com.googlecode.concurrentlinkedhashmap:concurrentlinkedhashmap-lru:1.3",
    sha1 = "beb907bae0604fdc153cbcc2f0dc84d3ae35bf36",
)

maven_jar(
    name = "com_clearspring_analytics_stream",
    artifact = "com.clearspring.analytics:stream:2.9.5",
    sha1 = "422b3fd1a2a38dc9ff9c9a2f6db9bda80962475e",
)

maven_jar(
    name = "org_apache_pdfbox_pdfbox",
    artifact = "org.apache.pdfbox:pdfbox:2.0.6",
    sha1 = "68616a583c5f9b9ba72140364d15a07cd937ce0e"
)

maven_jar(
    name = "org_apache_pdfbox_fontbox",
    artifact = "org.apache.pdfbox:fontbox:2.0.0",
    sha1 = "6f762d4e1c8ea99589d30597ef3731dfdcee43e2"
)

maven_jar(
    name = "com_googlecode_libphonenumber",
    artifact = "com.googlecode.libphonenumber:libphonenumber:8.8.4",
    sha1 = "1935df1efd2fcdd7c2e7d4f8cc08f245a2798346"
)

maven_jar(
    name = "com_nimbusds_srp6a",
    artifact = "com.nimbusds:srp6a:2.0.2",
    sha1 = "fc461127a39208502518ccbe51100c315e7625e8"
)

maven_jar(
    name = "com_auth0_java_jwt",
    artifact = "com.auth0:java-jwt:3.3.0",
    sha1 = "0e180a4b31f14c2a1cf203f457fb2149d2f6c1d2"
)

maven_jar(
    name = "com_sproutsocial_nsqj_j",
    artifact = "com.sproutsocial:nsq-j:0.9.1",
    sha1 = "4d51d71be5236e737b178716b226bb87a03949c7"
)

maven_jar(
    name = "net_sourceforge_lept4j",
    artifact = "net.sourceforge.lept4j:lept4j:1.10.0",
    sha1 = "72153b28e8e1f0391afcc2380c41ac8e73bd599e"
)

maven_jar(
    name = "net_sourceforge_tess4j",
    artifact = "net.sourceforge.tess4j:tess4j:4.0.2",
    sha1 = "95516b133368840a0974ef5316fedd9c5e3aa635"
)

maven_jar(
    name = "com_sun_media_jai_imageio",
    artifact = "com.github.jai-imageio:jai-imageio-core:1.4.0",
    sha1 = "fb6d79b929556362a241b2f65a04e538062f0077"
)
