"""
Import the new Skylark version of http_archive & git_repository

This uses the system's native git client which supports fancy key formats and key passphrases.
"""
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")

"""
Assert Bazel version

Usually this should be set to the version of Bazel used for CI
"""
git_repository(
    name = "bazel_skylib",
    remote = "https://github.com/bazelbuild/bazel-skylib",
    commit = "6126842e3db2ec4986752f6dfc0860ca922997f1",
    shallow_since = "1557756873 +0200"
)
load("@bazel_skylib//lib:versions.bzl", "versions")
versions.check("0.25.0")

"""
Import bazel-common, which has a Maven pom_file generation rule
"""
BAZEL_COMMON_TAG = "f3dc1a775d21f74fc6f4bbcf076b8af2f6261a69"
BAZEL_COMMON_SHA = "ccdd09559b49c7efd9e4b0b617b18e2a4bbdb2142fc30dfd3501eb5fa1294dcc"
http_archive(
    name = "bazel_common",
    strip_prefix = "bazel-common-%s" % BAZEL_COMMON_TAG,
    sha256 = BAZEL_COMMON_SHA,
    url = "https://github.com/google/bazel-common/archive/%s.zip" % BAZEL_COMMON_TAG,
)

"""
Import rules_jvm_external for better Maven support
"""
RULES_JVM_EXTERNAL_TAG = "2.2"
RULES_JVM_EXTERNAL_SHA = "f1203ce04e232ab6fdd81897cf0ff76f2c04c0741424d192f28e65ae752ce2d6"
http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)
load("@rules_jvm_external//:defs.bzl", "maven_install", "artifact")
load("@rules_jvm_external//:specs.bzl", "maven")

"""
Install Maven dependencies
"""
load("//secrets:secrets.bzl", "RT_USERNAME", "RT_PASSWORD")

maven_install(
    name = "maven",
    artifacts = [
        "asm:asm:3.1",
        "c3p0:c3p0:0.9.1.1",
        "ch.qos.logback.contrib:logback-jackson:0.1.5",
        "ch.qos.logback.contrib:logback-json-classic:0.1.5",
        "ch.qos.logback.contrib:logback-json-core:0.1.5",
        "ch.qos.logback:logback-classic:1.1.11",
        "com.amazonaws:aws-java-sdk-code-generator:1.11.414",
        #"com.amazonaws:aws-java-sdk-core:1.11.414-SNAPSHOT",
        "com.amazonaws:aws-java-sdk-kms:1.11.414",
        "com.amazonaws:aws-java-sdk-s3:1.11.414",
        #"com.amazonaws:aws-java-sdk-sqs:1.11.414-SNAPSHOT",
        "com.amazonaws:jmespath-java:1.11.414",
        "com.auth0:java-jwt:3.3.0",
        "com.codahale.metrics:metrics-healthchecks:3.0.2",
        maven.artifact(
            group = "com.datastax.cassandra",
            artifact = "cassandra-driver-core",
            version = "3.4.0",
            exclusions = [
                "io.dropwizard.metrics:metrics-core",
            ]
        ),
        "com.fasterxml.jackson.core:jackson-annotations:2.9.7",
        "com.fasterxml.jackson.core:jackson-core:2.9.7",
        "com.fasterxml.jackson.core:jackson-databind:2.9.7",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.9.7",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.9.7",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.9.7",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.7",
        "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.9.7",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-base:2.9.7",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.9.7",
        "com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.9.7",
        "com.github.detro.ghostdriver:phantomjsdriver:1.0.3",
        "com.github.rholder:guava-retrying:2.0.0",
        "com.github.tomakehurst:wiremock-standalone:2.1.12",
        "com.google.code.findbugs:jsr305:3.0.0",
        "com.google.code.gson:gson:2.2.2",
        "com.google.errorprone:error_prone_annotations:2.0.11",
        "com.google.errorprone:javac-shaded:9+181-r4173-1",
        "com.google.googlejavaformat:google-java-format:1.7",
        "com.google.guava:guava:21.0",
        "com.google.http-client:google-http-client:1.17.0-rc",
        "com.google.inject.extensions:guice-multibindings:4.1.0",
        "com.google.inject:guice:4.1.0",
        "com.google.instrumentation:instrumentation-api:0.4.3",
        "com.google.zxing:core:3.3.3",
        "com.google.zxing:javase:3.3.3",
        "com.googlecode.gettext-commons:gettext-commons:0.9.8",
        "com.googlecode.libphonenumber:libphonenumber:5.7",
        "com.lambdaworks:scrypt:1.3.2",
        "com.netflix.governator:governator:1.17.2",
        "com.nimbusds:srp6a:2.0.2",
        "com.sparkjava:spark-core:2.8.0",
        "com.sun.jersey.contribs:jersey-apache-client4:1.18.1",
        "com.sun.jersey:jersey-client:1.18.1",
        "com.sun.jersey:jersey-core:1.18.1",
        "com.sun.jersey:jersey-server:1.18.1",
        "com.yubico:yubico-validation-client2:2.0.1",
        "commons-cli:commons-cli:1.4",
        "commons-codec:commons-codec:1.10",
        "commons-httpclient:commons-httpclient:3.1",
        "commons-io:commons-io:2.5",
        "commons-lang:commons-lang:2.6",
        "de.jollyday:jollyday:0.4.7",
        "eu.geekplace.javapinning:java-pinning-jar:1.0.1",
        "io.grpc:grpc-core:1.16.1",
        "io.grpc:grpc-netty:1.16.1",
        "io.netty:netty-codec-http2:4.1.30.Final",
        "io.netty:netty-codec:4.1.30.Final",
        "io.netty:netty-tcnative-boringssl-static:2.0.17.Final",
        "io.prometheus:simpleclient:0.0.19",
        "io.prometheus:simpleclient:0.5.0",
        "io.prometheus:simpleclient_common:0.0.19",
        "io.prometheus:simpleclient_hotspot:0.0.19",
        "io.prometheus:simpleclient_servlet:0.0.19",
        "io.protostuff:protostuff-api:1.5.1",
        "io.protostuff:protostuff-core:1.5.1",
        "io.protostuff:protostuff-runtime:1.5.1",
        "io.swagger:swagger-annotations:1.5.9",
        "io.takari.junit:takari-cpsuite:1.2.7",
        "io.vavr:vavr-jackson:0.10.0",
        "io.vavr:vavr-match:0.10.0",
        "io.vavr:vavr-test:0.10.0",
        "io.vavr:vavr:0.10.0",
        "javax.inject:javax.inject:1",
        "javax.inject:javax.inject:1",
        "javax.servlet:javax.servlet-api:3.0.1",
        "javax.validation:validation-api:1.1.0.Final",
        "joda-time:joda-time:2.9.9",
        "junit:junit:4.12",
        "mysql:mysql-connector-java:5.1.42",
        "net.sourceforge.argparse4j:argparse4j:0.4.3",
        "net.sourceforge.cssparser:cssparser:0.9.8",
        "net.spy:spymemcached:2.9.1",
        "org.apache.commons:commons-collections4:4.0",
        "org.apache.commons:commons-csv:1.0",
        "org.apache.commons:commons-lang3:3.4",
        "org.apache.commons:commons-math3:3.4.1",
        "org.apache.curator:curator-client:4.0.1",
        "org.apache.curator:curator-framework:4.0.1",
        "org.apache.curator:curator-recipes:4.0.1",
        "org.apache.curator:curator-x-discovery:4.0.1",
        "org.apache.httpcomponents:httpclient:4.5.1",
        "org.apache.httpcomponents:httpcore:4.3.2",
        "org.apache.logging.log4j:log4j-api:2.11.1",
        "org.apache.logging.log4j:log4j-core:2.11.1",
        #"org.apache.logging.log4j:log4j-slf4j-impl:2.11.1",
        "org.apache.pdfbox:fontbox:2.0.0",
        "org.apache.pdfbox:pdfbox:2.0.6",
        "org.apache.velocity:velocity:1.7",
        "org.assertj:assertj-core:2.2.0",
        "org.bouncycastle:bcpkix-jdk15on:1.59",
        "org.bouncycastle:bcprov-jdk15on:1.59",
        "org.codehaus.plexus:plexus-utils:3.0.17",
        "org.eclipse.jetty:jetty-http:9.0.7.v20131107",
        "org.eclipse.jetty:jetty-server:9.0.7.v20131107",
        "org.eclipse.jetty:jetty-servlet:9.0.7.v20131107",
        "org.eclipse.jetty:jetty-servlets:9.0.7.v20131107",
        "org.hamcrest:hamcrest-core:1.3",
        "org.hamcrest:hamcrest-library:1.3",
        "org.hibernate:hibernate-annotations:3.5.4-Final",
        "org.hibernate:hibernate-entitymanager:3.5.4-Final",
        "org.hibernate:hibernate-validator:5.1.1.Final",
        "org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.0.Final",
        "org.iban4j:iban4j:3.1.0",
        "org.javassist:javassist:3.21.0-GA",
        "org.json:json:20080701",
        "org.jsoup:jsoup:1.7.2",
        "org.mockito:mockito-core:2.2.22",
        "org.modelmapper:modelmapper:1.1.0",
        "org.mozilla:rhino:1.7R4",
        "org.pojava:pojava:2.8.1",
        "org.reflections:reflections:0.9.9-RC2",
        "org.seleniumhq.selenium:selenium-api:2.29.0",
        "org.slf4j:jul-to-slf4j:1.7.6",
        "org.slf4j:slf4j-api:1.7.21",
        "org.springframework.data:spring-data-commons:1.13.1.RELEASE",
        "org.springframework.data:spring-data-jpa:1.11.1.RELEASE",
        "org.springframework.security:spring-security-core:4.2.3.RELEASE",
        "org.springframework:spring-context:4.3.7.RELEASE",
        "org.springframework:spring-core:4.3.7.RELEASE",
        "org.springframework:spring-expression:4.3.7.RELEASE",
        "org.springframework:spring-orm:4.3.7.RELEASE",
        "org.springframework:spring-tx:4.3.7.RELEASE",
        "org.xerial.snappy:snappy-java:1.0.5-M2",
        "org.yaml:snakeyaml:1.13",
        "pl.pragmatists:JUnitParams:1.0.5",
        #"tink.org.apache.httpcomponents:httpclient:4.5.5-SNAPSHOT",
        #"tink.org.apache.httpcomponents:httpcore:4.4.9-SNAPSHOT",
        "xerces:xercesImpl:2.10.0",
    ],
    repositories = [
        "https://%s:%s@tinkab.jfrog.io/tinkab/libs-snapshot/" % (RT_USERNAME, RT_PASSWORD),
        "https://repo.maven.apache.org/maven2/",
    ],
)

"""
Import Docker rule
"""
IO_BAZEL_RULES_DOCKER_TAG = "3332026921c918c9bfaa94052578d0ca578aab66"
IO_BAZEL_RULES_DOCKER_SHA = "8729112ed4288143955d50fee6c5a306305b9fca48838f73efc1f21f3c32573f"
http_archive(
    name = "io_bazel_rules_docker",
    strip_prefix = "rules_docker-%s" % IO_BAZEL_RULES_DOCKER_TAG,
    sha256 = IO_BAZEL_RULES_DOCKER_SHA,
    url = "https://github.com/bazelbuild/rules_docker/archive/%s.zip" % IO_BAZEL_RULES_DOCKER_TAG,
)
load("@io_bazel_rules_docker//repositories:repositories.bzl", container_repositories = "repositories")
container_repositories()
load("@io_bazel_rules_docker//container:container.bzl","container_pull")

"""
Pull Docker images
"""
container_pull(
    name = "openjdk_jdk8",
    registry = "gcr.io",
    repository = "tink-containers/openjdk-8-jre",
    tag = "8",
)

"""
Tink-vendored dependencies

These are repositories under Tink control. They are trusted, and imported
as a part of tink-backend's repository (not checksumed).
"""
git_repository(
    name = "dropwizard_jersey",
    commit = "0c2f90f4358e262d0fe0af3f6d31eb0fa3cabc40",
    remote = "git@github.com:tink-ab/dropwizard.git",
    shallow_since = "1490898663 +0200",
)

git_repository(
    name = "tink_httpcore_4_4_9",
    remote = "git@github.com:tink-ab/httpcomponents-core.git",
    commit = "0f72fa2c392fee8388d327cb3462cd10d675c2e2",
    shallow_since = "1537528950 +0200",
)

git_repository(
    name = "tink_httpclient_4_5_5",
    remote = "git@github.com:tink-ab/httpcomponents-client.git",
    commit = "1ed65fa09a4b7bc9f469fbb3625ac5b087f9cc3e",
    shallow_since = "1537529121 +0200",
)

git_repository(
    name = "tink_aws_sdk_1_11",
    remote = "git@github.com:tink-ab/aws-sdk-java.git",
    commit = "1bd88709966b245373b4b71f5bca4c0d7202bf1a",
    shallow_since = "1543992595 +0100",
)

git_repository(
    name = "tink_backend_shared_libraries",
    remote = "git@github.com:tink-ab/tink-backend-shared-libraries",
    commit = "cecd27397f7d35b188d960cbc11b737e46f5ad7d",
)

"""
External repositories that are not under Tink control.

These should *always* be locked to something stable
(a commit or a release tag, but not a branch for example) and have
a checksum to prevent tampering by the remote end.
"""
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
    sha256 = "9510dd2afc29e7245e9e884336f848c8a6600a14ae726adb6befdb4f786f0be2",
    strip_prefix = "protobuf-3.6.1.3",
    urls = ["https://github.com/google/protobuf/archive/v3.6.1.3.zip"],
)

bind(
    name = "protocol_compiler",
    actual = "@com_google_protobuf//:protoc",
)

"""
Import Maven packages which rules_jvm_external can't currently resolve
"""
# libm4ri library, needed by https://github.com/tink-ab/tink-backend-aggregation/tree/master/tools/libkbc_wbaes_src
http_file(
    name =  "libm4ri_dev",
    downloaded_file_path = "libm4ri-dev_20140914-2+b1_amd64.deb",
    urls = ["http://ftp.br.debian.org/debian/pool/main/libm/libm4ri/libm4ri-dev_20140914-2+b1_amd64.deb"],
    sha256 = "040b81df10945380424d8874d38c062f45a5fee6886ae8e6963c87393ba84cd9",
)

http_file(
    name =  "libm4ri_0.0.20140914",
    downloaded_file_path = "libm4ri-0.0.20140914_20140914-2+b1_amd64.deb",
    urls = ["http://ftp.br.debian.org/debian/pool/main/libm/libm4ri/libm4ri-0.0.20140914_20140914-2+b1_amd64.deb"],
    sha256 = "c2f38d51730b6e9a73e2f4d2e0edfadf647a9889da9d06a15abca07d3eccc6f1",
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
    name = "com_google_instrumentation_instrumentation_api",
    artifact = "com.google.instrumentation:instrumentation-api:0.4.3",
    sha1 = "41614af3429573dc02645d541638929d877945a2",
)

maven_jar(
    name = "com_google_errorprone_error_prone_annotations",
    artifact = "com.google.errorprone:error_prone_annotations:2.0.11",
    sha1 = "3624d81fca4e93c67f43bafc222b06e1b1e3b260",
)

maven_jar(
    name = "org_eclipse_jetty_toolchain_setuid_jetty_setuid_java",
    artifact = "org.eclipse.jetty.toolchain.setuid:jetty-setuid-java:1.0.2",
    sha1 = "4dc7fca46ac6badff4336c574b2c713ac3e40f73",
)

maven_jar(
    name = "com_codahale_metrics_metrics_httpclient",
    artifact = "com.codahale.metrics:metrics-httpclient:3.0.2",
    sha1 = "c658daf41b1ecf934ccd21e83eeeb18703355afb",
)

maven_jar(
    name = "com_google_googlejavaformat",
    artifact = "com.google.googlejavaformat:google-java-format:1.7",
    sha1 = "97cb6afc835d65682edc248e19170a8e4ecfe4c4"
)

maven_jar(
    name = "com_google_errorprone",
    artifact  = "com.google.errorprone:javac-shaded:9+181-r4173-1",
    sha1 = "a399ee380b6d6b6ea53af1cfbcb086b108d1efb7"
)

maven_jar(
    name = "software_amazon_ion_ion_java",
    artifact = "software.amazon.ion:ion-java:1.0.2",
    sha1 = "ee9dacea7726e495f8352b81c12c23834ffbc564",
)

maven_jar(
    name = "com_fasterxml_jackson_dataformat_jackson_dataformat_cbor",
    artifact = "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.9.7",
    sha1 = "3c4fd27ba1e87ccdd32fa336216f2cc1e04f5c20",
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
    name = "net_java_dev_jna_platform",
    artifact = "net.java.dev.jna:jna-platform:4.5.1",
    sha1 = "117d52c9f672d8b7ea80a81464c33ef843de9254",
)

maven_jar(
    name = "com_fasterxml_jackson_core_jackson_databind",
    artifact = "com.fasterxml.jackson.core:jackson-databind:2.9.7",
    sha1 = "e6faad47abd3179666e89068485a1b88a195ceb7",
)

maven_jar(
    name = "xmlpull_xmlpull",
    artifact = "xmlpull:xmlpull:1.1.3.1",
    sha1 = "2b8e230d2ab644e4ecaa94db7cdedbc40c805dfa",
)

maven_jar(
    name = "com_fasterxml_jackson_core_jackson_core",
    artifact = "com.fasterxml.jackson.core:jackson-core:2.9.7",
    sha1 = "4b7f0e0dc527fab032e9800ed231080fdc3ac015",
)

maven_jar(
    name = "org_apache_httpcomponents_httpmime",
    artifact = "org.apache.httpcomponents:httpmime:4.2.2",
    sha1 = "817d56b6f7c8af1fc90b5dbb78fb1125180fc3f3",
)

maven_jar(
    name = "c",
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
    artifact = "com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.9.7",
    sha1 = "2774b8e960697678ca87cf54abd59c736fcd1e83",
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
    name = "io_netty_netty_buffer",
    artifact = "io.netty:netty-buffer:4.1.30.Final",
    sha1 = "597adb653306470fb3ec1af3c0f3f30a37b1310a",
)

maven_jar(
    name = "io_netty_netty_codec",
    artifact = "io.netty:netty-codec:4.1.30.Final",
    sha1 = "515c8f609aaca28a94f984d89a9667dd3359c1b1",
)

maven_jar(
    name = "io_netty_netty_codec_dns",
    artifact = "io.netty:netty-codec-dns:4.1.30.Final",
    sha1 = "7d28ce324f6cd5ae4ddd7f3e5027e2a7f126740b",
)

maven_jar(
    name = "io_netty_netty_codec_http",
    artifact = "io.netty:netty-codec-http:4.1.30.Final",
    sha1 = "1384c630e8a0eeef33ad12a28791dce6e1d8767c",
)

maven_jar(
    name = "io_netty_netty_codec_http2",
    artifact = "io.netty:netty-codec-http2:4.1.30.Final",
    sha1 = "2da92f518409904954d3e8dcc42eb6a562a70302",
)

maven_jar(
    name = "io_netty_netty_codec_socks",
    artifact = "io.netty:netty-codec-socks:4.1.30.Final",
    sha1 = "ea272e3bb281d3a91d27278f47e61b4de285cc27",
)

maven_jar(
    name = "io_netty_netty_common",
    artifact = "io.netty:netty-common:4.1.30.Final",
    sha1 = "5dca0c34d8f38af51a2398614e81888f51cf811a",
)

maven_jar(
    name = "io_netty_netty_handler",
    artifact = "io.netty:netty-handler:4.1.30.Final",
    sha1 = "ecc076332ed103411347f4806a44ee32d9d9cb5f",
)

maven_jar(
    name = "io_netty_netty_handler_proxy",
    artifact = "io.netty:netty-handler-proxy:4.1.30.Final",
    sha1 = "1baa1568fa936caddca0fae96fdf127fd5cbad16",
)

maven_jar(
    name = "io_netty_netty_resolver",
    artifact = "io.netty:netty-resolver:4.1.30.Final",
    sha1 = "5106fd687066ffd712e5295d32af4e2ac6482613",
)

maven_jar(
    name = "io_netty_netty_resolver_dns",
    artifact = "io.netty:netty-resolver-dns:4.1.30.Final",
    sha1 = "3f4bcf2e9fff1361ac9ad0bd27a10a1b31399294",
)

maven_jar(
    name = "io_netty_netty_transport",
    artifact = "io.netty:netty-transport:4.1.30.Final",
    sha1 = "3d27bb432a3b125167ac161b26415ad29ec17f02",
)

maven_jar(
    name = "javax_el_javax_el_api",
    artifact = "javax.el:javax.el-api:2.2.5",
    sha1 = "370140e991eefb212a6d6baedbce585f00ef76e0",
)

maven_jar(
    name = "com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
    artifact = "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.9.7",
    sha1 = "134f1518a9516be01b25526a935d4fd68610b89c",
)

maven_jar(
    name = "log4j_log4j",
    artifact = "log4j:log4j:1.2.14",
    sha1 = "03b254c872b95141751f414e353a25c2ac261b51",
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
    name = "com_github_detro_ghostdriver_phantomjsdriver",
    artifact = "com.github.detro.ghostdriver:phantomjsdriver:1.0.3",
    sha1 = "17fdf2fe6bb281a7d37fdcb0fd87c77a78bf0d76",
)

maven_jar(
    name = "com_fasterxml_jackson_core_jackson_annotations",
    artifact = "com.fasterxml.jackson.core:jackson-annotations:2.9.7",
    sha1 = "4b838e5c4fc17ac02f3293e9a558bb781a51c46d",
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
    name = "c3p0_c3p0",
    artifact = "c3p0:c3p0:0.9.1.1",
    sha1 = "302704f30c6e7abb7a0457f7771739e03c973e80",
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
    name = "com_fasterxml_jackson_datatype_jackson_datatype_guava",
    artifact = "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.9.7",
    sha1 = "3fc843def28bb7b7f7fae414cc61b347ba89e5ce",
)

maven_jar(
    name = "net_java_dev_jna_jna",
    artifact = "net.java.dev.jna:jna:4.5.1",
    sha1 = "65bd0cacc9c79a21c6ed8e9f588577cd3c2f85b9",
)

maven_jar(
    name = "javax_inject_javax_inject",
    artifact = "javax.inject:javax.inject:1",
    sha1 = "6975da39a7040257bd51d21a231b76c915872d38",
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
    name = "org_glassfish_web_javax_el",
    artifact = "org.glassfish.web:javax.el:2.2.6",
    sha1 = "0232abfe1f919127e9cfcd6e4e2c6324bb394535",
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
    artifact = "com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.9.7",
    sha1 = "cc3a9b1dcd05d65469144026d895968a35c86198",
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
    name = "com_codahale_metrics_metrics_annotation",
    artifact = "com.codahale.metrics:metrics-annotation:3.0.2",
    sha1 = "49cee8ba1d76f67c59146546448fc0f3eef32b75",
)

maven_jar(
    name = "com_fasterxml_jackson_datatype_jackson_datatype_joda",
    artifact = "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.9.7",
    sha1 = "d9feae99faf03d5a9989be2225da3baa73ba43d4",
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
    name = "xml_apis_xml_apis",
    artifact = "xml-apis:xml-apis:1.0.b2",
    sha1 = "3136ca936f64c9d68529f048c2618bd356bf85c9",
)

maven_jar(
    name = "com_fasterxml_jackson_module_jackson_module_afterburner",
    artifact = "com.fasterxml.jackson.module:jackson-module-afterburner:2.9.7",
    sha1 = "8f864e3a2bb4da738d8cdc56f3825298bee8b943",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_servlets",
    artifact = "org.eclipse.jetty:jetty-servlets:9.0.7.v20131107",
    sha1 = "bffeae9b9f75f53d3e5dc1bfd56725f67f2f67c0",
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
    artifact = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.7",
    sha1 = "a428edc4bb34a2da98a50eb759c26941d4e85960",
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
    artifact = "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.9.7",
    sha1 = "fdbe6c2867454dabeda629e3e24a4c1a227fed23",
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
    name = "ch_qos_logback_logback_core",
    artifact = "ch.qos.logback:logback-core:1.1.11",
    sha1 = "88b8df40340eed549fb07e2613879bf6b006704d",
)

maven_jar(
    name = "com_codahale_metrics_metrics_jersey",
    artifact = "com.codahale.metrics:metrics-jersey:3.0.2",
    sha1 = "6ece3e06de45ee7a7e73b66ca36862f27ca88717",
)

maven_jar(
    name = "com_spatial4j_spatial4j",
    artifact = "com.spatial4j:spatial4j:0.3",
    sha1 = "2fd58d1de64553db427ae4fbc1a6691adea460e2",
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
    name = "com_amazonaws_aws_java_sdk_s3",
    artifact = "com.amazonaws:aws-java-sdk-s3:1.11.381",
    sha1 = "a2600515a420a5fd08d5c1d19fdf4a8714c515e2"
    )

maven_jar(
    name = "com_amazonaws_aws_java_sdk_kms",
    artifact = "com.amazonaws:aws-java-sdk-kms:1.11.381",
    sha1 = "17a06d9854f804dfcf2ec799f906f555984084c9"
)

maven_jar(
    name = "org_slf4j_jcl_over_slf4j",
    artifact = "org.slf4j:jcl-over-slf4j:1.7.6",
    sha1 = "ab1648fe1dd6f1e5c2ec6d12f394672bb8c1036a",
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
    name = "commons_logging_commons_logging",
    artifact = "commons-logging:commons-logging:1.1.3",
    sha1 = "f6f66e966c70a83ffbdb6f17a0919eaf7c8aca7f",
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
    name = "org_apache_mahout_mahout_core",
    artifact = "org.apache.mahout:mahout-core:0.9",
    sha1 = "22a63210e10d39be2edcc01564f5d5f68f91eeb7",
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
    name = "xml_apis_xml_apis_ext",
    artifact = "xml-apis:xml-apis-ext:1.3.04",
    sha1 = "41a8b86b358e87f3f13cf46069721719105aff66",
)

maven_jar(
    name = "com_fasterxml_classmate",
    artifact = "com.fasterxml:classmate:1.0.0",
    sha1 = "434efef28c81162b17c540e634cffa3bd9b09b4c",
)

maven_jar(
    name = "commons_collections_commons_collections",
    artifact = "commons-collections:commons-collections:3.1",
    sha1 = "40fb048097caeacdb11dbb33b5755854d89efdeb",
)

maven_jar(
    name = "net_jcip_jcip_annotations",
    artifact = "net.jcip:jcip-annotations:1.0",
    sha1 = "afba4942caaeaf46aab0b976afd57cc7c181467e",
)

maven_jar(
    name = "com_fasterxml_jackson_jaxrs_jackson_jaxrs_base",
    artifact = "com.fasterxml.jackson.jaxrs:jackson-jaxrs-base:2.9.7",
    sha1 = "0d7f607b3a12e6e92b2f29c721e03c11b638cc32",
)

maven_jar(
    name = "org_springframework_data_spring_data_commons",
    artifact = "org.springframework.data:spring-data-commons:1.13.1.RELEASE",
    sha1 = "4e4257f2eb3f191613b4b000d43e8d0c3ff4457e",
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
    name = "org_apache_curator_curator_framework",
    artifact = "org.apache.curator:curator-framework:4.0.1",
    sha1 = "3da85d2bda41cb43dc18c089820b67d12ba38826",
)

maven_jar(
    name = "com_codahale_metrics_metrics_json",
    artifact = "com.codahale.metrics:metrics-json:3.0.2",
    sha1 = "46198fe0284104519b60ff2ad49f71c98ba942f2",
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
    name = "net_sourceforge_lept4j_lept4j",
    artifact = "net.sourceforge.lept4j:lept4j:1.10.0",
    sha1 = "72153b28e8e1f0391afcc2380c41ac8e73bd599e"
)

maven_jar(
    name = "net_sourceforge_tess4j_tess4j",
    artifact = "net.sourceforge.tess4j:tess4j:4.0.2",
    sha1 = "95516b133368840a0974ef5316fedd9c5e3aa635"
)

maven_jar(
    name = "com_sun_media_jai_imageio",
    artifact = "com.github.jai-imageio:jai-imageio-core:1.4.0",
    sha1 = "fb6d79b929556362a241b2f65a04e538062f0077"
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

"""
TODO: Remove all prometheus imports below once tink-backend-shared-libraries starts
using rules_jvm_external/maven_install as well.
"""
maven_jar(
    name = "io_prometheus_simpleclient_hotspot",
    artifact = "io.prometheus:simpleclient_hotspot:0.0.19",
    sha1 = "4cd4d60a9f06922f23e589f9cf520cd1d6989aad",
)

maven_jar(
    name = "io_prometheus_simpleclient_common",
    artifact = "io.prometheus:simpleclient_common:0.0.19",
    sha1 = "aa0d4a87c02e71924c913fbb4629b7ca5966a5ff",
)

maven_jar(
    name = "io_prometheus_simpleclient",
    artifact = "io.prometheus:simpleclient:0.0.19",
    sha1 = "c1424b444a7ec61e056a180d52470ff397bc428d",
)

maven_jar(
    name = "io_prometheus_simpleclient_servlet",
    artifact = "io.prometheus:simpleclient_servlet:0.0.19",
    sha1 = "f7ed8e8f32aafbe9e918f4c8fcec7f99dee9b6f9",
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

"""
Legacy Dropwizard deps

Doesn't seem to work properly via rules_jvm_external/maven_install
"""
maven_jar(
    name = "com_sun_jersey_jersey_client",
    artifact = "com.sun.jersey:jersey-client:1.18.1",
    sha1 = "60e85d4f638fb444bcbbd9f8da83414fbdf731c3",
)

maven_jar(
    name = "com_sun_jersey_jersey_core",
    artifact = "com.sun.jersey:jersey-core:1.18.1",
    sha1 = "4f97b0f85a881d3b3478f99a17df7bd258b9d626",
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
    name = "io_dropwizard_dropwizard_core",
    artifact = "io.dropwizard:dropwizard-core:0.7.1",
    sha1 = "569cd6181d5fea4b033cf3a0eca632c8931a1a3a",
)

maven_jar(
    name = "io_dropwizard_dropwizard_client",
    artifact = "io.dropwizard:dropwizard-client:0.7.1",
    sha1 = "7cef9884395e90a4dc627e643f84b48a2ff6fb30",
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
    name = "io_dropwizard_dropwizard_validation",
    artifact = "io.dropwizard:dropwizard-validation:0.7.1",
    sha1 = "59a73cdcab2489b6dd342926bb4250e9c62e6938",
)

maven_jar(
    name = "io_dropwizard_dropwizard_metrics",
    artifact = "io.dropwizard:dropwizard-metrics:0.7.1",
    sha1 = "6ca1d7d1d1d1bcf7c803a127e0c6696d1c98fdb1",
)

maven_jar(
    name = "io_dropwizard_dropwizard_jackson",
    artifact = "io.dropwizard:dropwizard-jackson:0.7.1",
    sha1 = "6bc655d16acdb556001b1d677fab6c69f7ba296a",
)

maven_jar(
    name = "io_dropwizard_dropwizard_util",
    artifact = "io.dropwizard:dropwizard-util:0.7.1",
    sha1 = "49c3b2764b4f6ad1a97ae7148ac8a640488b0b29",
)

maven_jar(
    name = "io_dropwizard_dropwizard_configuration",
    artifact = "io.dropwizard:dropwizard-configuration:0.7.1",
    sha1 = "161539b50579dffbcd00eff796adc2020c02a6f9",
)

maven_jar(
    name = "io_dropwizard_dropwizard_lifecycle",
    artifact = "io.dropwizard:dropwizard-lifecycle:0.7.1",
    sha1 = "12c282d9682628d7c33654e0d62f1dbf2a883d20",
)

maven_jar(
    name = "io_dropwizard_dropwizard_servlets",
    artifact = "io.dropwizard:dropwizard-servlets:0.7.1",
    sha1 = "b9d2016bbeb2df39cc90632338923273d708b55e",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_http",
    artifact = "org.eclipse.jetty:jetty-http:9.0.7.v20131107",
    sha1 = "67060a59b426c76a2788ea5f4e19c1d3170ac562",
)

maven_jar(
    name = "org_eclipse_jetty_jetty_server",
    artifact = "org.eclipse.jetty:jetty-server:9.0.7.v20131107",
    sha1 = "682ae23f9e4a5e397d96f215b62641755d2a59b7",
)

maven_jar(
    name = "org_hibernate_hibernate_validator",
    artifact = "org.hibernate:hibernate-validator:5.1.1.Final",
    sha1 = "2bd44618dc13c2be39231776a0edf0e1f867dedc",
)

maven_jar(
    name = "org_yaml_snakeyaml",
    artifact = "org.yaml:snakeyaml:1.13",
    sha1 = "73cbb494a912866c4c831a178c3a2a9169f4eaad",
)

maven_jar(
    name = "net_sourceforge_argparse4j_argparse4j",
    artifact = "net.sourceforge.argparse4j:argparse4j:0.4.3",
    sha1 = "f4c256934d79940477a35b6c4c182b47ee5f0a6f",
)

### === END === Java Spark dependencies

"""
Import GRPC/Protobuf rules
"""
http_archive(
    name = "build_stack_rules_proto",
    sha256 = "0d88313ba32c0042c2633c3cbdd187afb0c3c9468b978f6eb4919ac6e535f029",
    strip_prefix = "rules_proto-8afa882b3dff5fec93b22519d34d0099083a7ce2",
    urls = ["https://github.com/stackb/rules_proto/archive/8afa882b3dff5fec93b22519d34d0099083a7ce2.tar.gz"],
)
http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "9d23d9fec84e24bd3962f5ef9d1fd61ce939d3f649a22bcab0f19e8167fae8ef",
    strip_prefix = "grpc-java-1.20.0",
    urls = [
        "https://github.com/grpc/grpc-java/archive/v1.20.0.zip",
    ],
)
load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")
grpc_java_repositories(omit_com_google_protobuf = True)
