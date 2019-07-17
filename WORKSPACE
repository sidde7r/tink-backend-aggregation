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
    commit = "6126842e3db2ec4986752f6dfc0860ca922997f1",
    remote = "https://github.com/bazelbuild/bazel-skylib",
    shallow_since = "1557756873 +0200",
)

load("@bazel_skylib//lib:versions.bzl", "versions")

versions.check("0.25.0")

"""
Import bazel-common, which has a Maven pom_file generation rule
"""

BAZEL_COMMON_VERSION = "f3dc1a775d21f74fc6f4bbcf076b8af2f6261a69"

http_archive(
    name = "bazel_common",
    sha256 = "ccdd09559b49c7efd9e4b0b617b18e2a4bbdb2142fc30dfd3501eb5fa1294dcc",
    strip_prefix = "bazel-common-%s" % BAZEL_COMMON_VERSION,
    url = "https://github.com/google/bazel-common/archive/%s.zip" % BAZEL_COMMON_VERSION,
)

"""
Import rules_jvm_external for better Maven support
"""

RULES_JVM_EXTERNAL_VERSION = "2.5"

http_archive(
    name = "rules_jvm_external",
    sha256 = "249e8129914be6d987ca57754516be35a14ea866c616041ff0cd32ea94d2f3a1",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_VERSION,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_VERSION,
)

load("@rules_jvm_external//:defs.bzl", "artifact", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

"""
Install Maven dependencies
"""

load("//secrets:secrets.bzl", "RT_PASSWORD", "RT_USERNAME")

maven_install(
    name = "maven",
    artifacts = [
        "asm:asm:3.1",
        "c3p0:c3p0:0.9.1.1",
        "ch.qos.logback.contrib:logback-jackson:0.1.5",
        "ch.qos.logback.contrib:logback-json-classic:0.1.5",
        "ch.qos.logback.contrib:logback-json-core:0.1.5",
        "ch.qos.logback:logback-classic:1.1.11",
        "ch.qos.logback:logback-core:1.1.11",
        "com.amazonaws:aws-java-sdk-code-generator:1.11.414",
        #"com.amazonaws:aws-java-sdk-core:1.11.414-SNAPSHOT",
        "com.amazonaws:aws-java-sdk-kms:1.11.414",
        "com.amazonaws:aws-java-sdk-s3:1.11.414",
        #"com.amazonaws:aws-java-sdk-sqs:1.11.414-SNAPSHOT",
        "com.amazonaws:jmespath-java:1.11.414",
        "com.auth0:java-jwt:3.3.0",
        "com.codahale.metrics:metrics-healthchecks:3.0.2",
        "com.fasterxml.jackson.core:jackson-annotations:2.9.9",
        "com.fasterxml.jackson.core:jackson-core:2.9.9",
        "com.fasterxml.jackson.core:jackson-databind:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.9",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-base:2.9.9",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.9.9",
        "com.fasterxml.jackson.module:jackson-module-afterburner:2.9.9",
        "com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.9.9",
        "com.fasterxml.uuid:java-uuid-generator:3.1.5",
        "com.github.detro.ghostdriver:phantomjsdriver:1.0.3",
        "com.github.rholder:guava-retrying:2.0.0",
        "com.github.tomakehurst:wiremock-standalone:2.1.12",
        "com.google.code.findbugs:jsr305:3.0.2",
        "com.google.code.gson:gson:2.7",
        "com.google.errorprone:error_prone_annotations:2.2.0",
        "com.google.errorprone:javac-shaded:9+181-r4173-1",
        "com.google.googlejavaformat:google-java-format:1.7",
        "com.google.guava:guava:27.0.1-jre",
        "com.google.http-client:google-http-client:1.17.0-rc",
        "com.google.inject.extensions:guice-multibindings:4.1.0",
        "com.google.inject:guice:4.1.0",
        "com.google.instrumentation:instrumentation-api:0.4.3",
        "com.google.zxing:core:3.3.3",
        "com.google.zxing:javase:3.3.3",
        "com.googlecode.gettext-commons:gettext-commons:0.9.8",
        "com.googlecode.libphonenumber:libphonenumber:5.7",
        "com.lambdaworks:scrypt:1.3.2",
        "com.nimbusds:srp6a:2.0.2",
        "com.sparkjava:spark-core:2.8.0",
        "com.sun.jersey.contribs:jersey-apache-client4:1.18.1",
        "com.sun.jersey:jersey-client:1.18.1",
        "com.sun.jersey:jersey-core:1.18.1",
        "com.sun.jersey:jersey-server:1.18.1",
        "com.yubico:yubico-validation-client2:2.0.1",
        "commons-cli:commons-cli:1.4",
        "commons-codec:commons-codec:1.11",
        "commons-httpclient:commons-httpclient:3.1",
        "commons-io:commons-io:2.5",
        "commons-lang:commons-lang:2.6",
        "de.jollyday:jollyday:0.4.7",
        "eu.geekplace.javapinning:java-pinning-jar:1.0.1",
        "io.netty:netty-codec-http2:4.1.30.Final",
        "io.netty:netty-codec:4.1.30.Final",
        "io.netty:netty-tcnative-boringssl-static:2.0.17.Final",
        "io.prometheus:simpleclient:0.0.19",
        "io.prometheus:simpleclient_common:0.0.19",
        "io.prometheus:simpleclient_hotspot:0.0.19",
        "io.prometheus:simpleclient_servlet:0.0.19",
        "io.protostuff:protostuff-api:1.5.1",
        "io.protostuff:protostuff-core:1.5.1",
        "io.protostuff:protostuff-runtime:1.5.1",
        "io.swagger:swagger-annotations:1.5.9",
        "io.swagger:swagger-core:1.5.9",
        "io.swagger:swagger-jaxrs:1.5.9",
        "io.takari.junit:takari-cpsuite:1.2.7",
        "io.vavr:vavr-jackson:0.10.0",
        "io.vavr:vavr-match:0.10.0",
        "io.vavr:vavr-test:0.10.0",
        "io.vavr:vavr:0.10.0",
        "javax.annotation:javax.annotation-api:1.3.2",
        "javax.inject:javax.inject:1",
        "javax.servlet:javax.servlet-api:3.1.0",
        "javax.validation:validation-api:1.1.0.Final",
        "joda-time:joda-time:2.9.9",
        "junit:junit:4.12",
        "mysql:mysql-connector-java:5.1.42",
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
        "org.apache.httpcomponents:httpclient:4.5.5",
        "org.apache.httpcomponents:httpcore:4.4.9",
        "org.apache.logging.log4j:log4j-api:2.11.1",
        "org.apache.logging.log4j:log4j-core:2.11.1",
        #"org.apache.logging.log4j:log4j-slf4j-impl:2.11.1",
        "org.apache.pdfbox:fontbox:2.0.6",
        "org.apache.pdfbox:pdfbox:2.0.6",
        "org.apache.velocity:velocity:1.7",
        "org.assertj:assertj-core:2.2.0",
        "org.bouncycastle:bcpkix-jdk15on:1.59",
        "org.bouncycastle:bcprov-jdk15on:1.59",
        "org.codehaus.plexus:plexus-utils:3.0.17",
        "org.eclipse.jetty:jetty-http:9.4.12.v20180830",
        "org.eclipse.jetty:jetty-server:9.4.12.v20180830",
        "org.eclipse.jetty:jetty-servlet:9.4.12.v20180830",
        "org.eclipse.jetty:jetty-servlets:9.4.12.v20180830",
        "org.hamcrest:hamcrest-core:1.3",
        "org.hamcrest:hamcrest-library:1.3",
        "org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.0.Final",
        "org.hibernate:hibernate-annotations:3.5.4-Final",
        "org.hibernate:hibernate-entitymanager:3.5.4-Final",
        "org.hibernate:hibernate-validator:5.1.1.Final",
        "org.iban4j:iban4j:3.1.0",
        "org.javassist:javassist:3.21.0-GA",
        "org.json:json:20080701",
        "org.jsoup:jsoup:1.7.2",
        "org.mockito:mockito-core:2.2.22",
        "org.modelmapper:modelmapper:1.1.0",
        "org.mozilla:rhino:1.7R4",
        "org.pojava:pojava:2.8.1",
        "org.reflections:reflections:0.9.10",
        "org.seleniumhq.selenium:selenium-api:2.31.0",
        "org.seleniumhq.selenium:selenium-support:2.31.0",
        "org.slf4j:jul-to-slf4j:1.7.6",
        "org.slf4j:slf4j-api:1.7.25",
        "org.springframework.data:spring-data-commons:1.13.1.RELEASE",
        "org.springframework.data:spring-data-jpa:1.11.1.RELEASE",
        "org.springframework.security:spring-security-core:4.2.3.RELEASE",
        "org.springframework:spring-context:4.3.9.RELEASE",
        "org.springframework:spring-core:4.3.9.RELEASE",
        "org.springframework:spring-expression:4.3.9.RELEASE",
        "org.springframework:spring-orm:4.3.9.RELEASE",
        "org.springframework:spring-tx:4.3.9.RELEASE",
        "org.xerial.snappy:snappy-java:1.0.5-M2",
        "org.yaml:snakeyaml:1.24",
        "pl.pragmatists:JUnitParams:1.0.5",
        #"tink.org.apache.httpcomponents:httpclient:4.5.5-SNAPSHOT",
        #"tink.org.apache.httpcomponents:httpcore:4.4.9-SNAPSHOT",
        "xerces:xercesImpl:2.10.0",
    ],
    maven_install_json = "//third_party:maven_install.json",
    repositories = [
        "https://repo1.maven.org/maven2/",
    ],
)

load("@maven//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

maven_install(
    name = "dropwizard",
    artifacts = [
        "com.fasterxml.jackson.core:jackson-annotations:2.9.9",
        "com.fasterxml.jackson.core:jackson-core:2.9.9",
        "com.fasterxml.jackson.core:jackson-databind:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.9.9",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-base:2.9.9",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.9.9",
        "com.fasterxml.jackson.module:jackson-module-afterburner:2.9.9",
        "com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.9.9",
        "io.dropwizard:dropwizard-core:0.7.1",
        "io.dropwizard:dropwizard-client:0.7.1",
        "io.dropwizard:dropwizard-jetty:0.7.1",
        "io.dropwizard:dropwizard-logging:0.7.1",
        "io.dropwizard:dropwizard-validation:0.7.1",
        "io.dropwizard:dropwizard-metrics:0.7.1",
        "io.dropwizard:dropwizard-jackson:0.7.1",
        "io.dropwizard:dropwizard-util:0.7.1",
        "io.dropwizard:dropwizard-configuration:0.7.1",
        "io.dropwizard:dropwizard-lifecycle:0.7.1",
        "io.dropwizard:dropwizard-servlets:0.7.1",
        "net.sourceforge.argparse4j:argparse4j:0.4.3",
        "org.yaml:snakeyaml:1.24",
        "com.netflix.governator:governator:1.17.8",
    ],
    generate_compat_repositories = True,
    maven_install_json = "//third_party:dropwizard_install.json",
    repositories = [
        "https://repo1.maven.org/maven2/",
    ],
)

load("@dropwizard//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

load("@dropwizard//:compat.bzl", "compat_repositories")

compat_repositories()

maven_install(
    name = "tink_aws_sdk",
    artifacts = [
        "com.fasterxml.jackson.core:jackson-annotations:2.8.8",
        "com.fasterxml.jackson.core:jackson-core:2.8.8",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.8.8",
        "com.fasterxml.jackson.core:jackson-databind:2.8.8.1",
        "commons-logging:commons-logging:1.2",
        "commons-codec:commons-codec:1.10",
        "joda-time:joda-time:2.9.9",
        "software.amazon.ion:ion-java:1.0.2",
        "com.amazonaws:aws-java-sdk-code-generator:1.11.381",
        "com.amazonaws:aws-java-sdk-code-generator:1.11.381",
        "com.amazonaws:aws-java-sdk-sqs:1.11.381",
        "com.amazonaws:jmespath-java:1.11.381",
    ],
    generate_compat_repositories = True,
    maven_install_json = "//third_party:tink_aws_sdk_install.json",
    repositories = [
        "https://repo1.maven.org/maven2/",
    ],
)

load("@tink_aws_sdk//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

load("@tink_aws_sdk//:compat.bzl", "compat_repositories")

compat_repositories()

maven_install(
    name = "other",
    artifacts = [
        "com.google.instrumentation:instrumentation-api:0.4.3",
        "com.codahale.metrics:metrics-jersey:3.0.2",
        "com.codahale.metrics:metrics-healthchecks:3.0.2",
        "com.codahale.metrics:metrics-servlets:3.0.2",
        maven.artifact(
            group = "com.datastax.cassandra",
            artifact = "cassandra-driver-core",
            version = "3.4.0",
            exclusions = [
                "io.dropwizard.metrics:metrics-core",
            ],
        ),
        "com.sun.jersey:jersey-core:1.18.1",
        "com.sun.jersey:jersey-server:1.18.1",
        "com.sun.jersey:jersey-servlet:1.18.1",
        "io.prometheus:simpleclient:0.5.0",
        "io.prometheus:simpleclient_common:0.5.0",
        "io.prometheus:simpleclient_hotspot:0.5.0",
        "io.prometheus:simpleclient_servlet:0.5.0",
        "io.prometheus:simpleclient_httpserver:0.5.0",
        "io.prometheus:simpleclient_pushgateway:0.5.0",
        "org.apache.logging.log4j:log4j-core:2.11.1",
        "org.apache.logging.log4j:log4j-api:2.11.1",
        "io.netty:netty-resolver-dns:4.1.30.Final",
        "io.netty:netty-codec-dns:4.1.30.Final",
        "org.eclipse.jetty.orbit:javax.servlet:3.0.0.v201112011016",
        "net.java.dev.jna:jna:4.5.1",
        "net.sourceforge.lept4j:lept4j:1.12.0",
        maven.artifact(
            group = "net.sourceforge.tess4j",
            artifact = "tess4j",
            version = "4.3.1",
            exclusions = [
                # "com.lowagie:itext:2.1.7" has a corrupt dependency on "bouncycastle:bctsp-jdk14:138"
                # instead of the correct "org.bouncycastle:bctsp-jdk14:1.38"

                # Can be removed once https://github.com/zippy1978/ghost4j/pull/66 is merged
                # and new releases of ghost4j + tess4j are published
                "bouncycastle:bctsp-jdk14",
            ],
        ),
        "org.bouncycastle:bctsp-jdk14:1.38",
        "com.google.guava:guava:21.0",
        "commons-logging:commons-logging:1.2",
    ],
    generate_compat_repositories = True,
    maven_install_json = "//third_party:other_install.json",
    repositories = [
        "https://repo1.maven.org/maven2/",
    ],
)

load("@other//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

load("@other//:compat.bzl", "compat_repositories")

compat_repositories()

PROTOBUF_VERSION = "3.9.0"

OPENCENSUS_VERSION = "0.21.0"

GRPC_JAVA_VERSION = "1.22.1"

GRPC_JAVA_NANO_VERSION = "1.21.0"

maven_install(
    name = "grpc",
    artifacts = [
        "com.google.protobuf:protobuf-java:%s" % PROTOBUF_VERSION,
        "io.grpc:grpc-api:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-core:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-netty:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-protobuf-lite:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-protobuf-nano:%s" % GRPC_JAVA_NANO_VERSION,
        "io.grpc:grpc-services:%s" % GRPC_JAVA_VERSION,
        "io.opencensus:opencensus-api:%s" % OPENCENSUS_VERSION,
        "io.opencensus:opencensus-contrib-grpc-metrics:%s" % OPENCENSUS_VERSION,
        "io.perfmark:perfmark-api:0.17.0",
    ],
    generate_compat_repositories = True,
    maven_install_json = "//third_party:grpc_install.json",
    repositories = [
        "https://repo1.maven.org/maven2/",
    ],
)

load("@grpc//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

load("@grpc//:compat.bzl", "compat_repositories")

compat_repositories()

"""
Import Docker rule
"""

IO_BAZEL_RULES_DOCKER_VERSION = "3332026921c918c9bfaa94052578d0ca578aab66"

http_archive(
    name = "io_bazel_rules_docker",
    sha256 = "8729112ed4288143955d50fee6c5a306305b9fca48838f73efc1f21f3c32573f",
    strip_prefix = "rules_docker-%s" % IO_BAZEL_RULES_DOCKER_VERSION,
    url = "https://github.com/bazelbuild/rules_docker/archive/%s.zip" % IO_BAZEL_RULES_DOCKER_VERSION,
)

load("@io_bazel_rules_docker//repositories:repositories.bzl", container_repositories = "repositories")

container_repositories()

load("@io_bazel_rules_docker//container:container.bzl", "container_pull")

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
    name = "tink_aws_sdk_1_11",
    commit = "1bd88709966b245373b4b71f5bca4c0d7202bf1a",
    remote = "git@github.com:tink-ab/aws-sdk-java.git",
    shallow_since = "1537529416 +0200",
)

git_repository(
    name = "tink_backend_shared_libraries",
    commit = "c69da74aaf68d93f764c2ab2b7d62665b0925f39",
    remote = "git@github.com:tink-ab/tink-backend-shared-libraries",
    shallow_since = "1563523445 +0000",
)

git_repository(
    name = "tink_backend_integration_openbanking",
    commit = "a8b0ba1109e0393332b3c9afc70bee4d0e67ff7a",
    remote = "git@github.com:tink-ab/tink-backend-integration-openbanking.git",
    shallow_since = "1562076445 +0000",
)

git_repository(
    name = "com_tink_api_grpc",
    commit = "f23aeafc40b0105ab41cc0aeb31de754bb450a06",
    remote = "git@github.com:tink-ab/tink-grpc.git",
    #shallow_since = "1562857859 +0000",
)

git_repository(
    name = "tink_backend",
    commit = "41147567649a64187c3f2b5a8c78ed8e43045888",
    remote = "git@github.com:tink-ab/tink-backend",
    shallow_since = "1563543596 +0000",
)

"""
Import Maven packages which rules_jvm_external can't currently resolve
"""

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

"""
Import GRPC/Protobuf rules
"""

http_file(
    name = "protoc_gen_grpc_java_linux_x86_64",
    sha256 = "6f5fc69224f2fa9ed7e1376aedf6c5c6239dcfe566beb89d3a1c77c50fb8886b",
    urls = ["http://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.2.0/protoc-gen-grpc-java-1.2.0-linux-x86_64.exe"],
)

http_file(
    name = "protoc_gen_grpc_java_macosx",
    sha256 = "f7ad13d42e2a2415d021263ae258ca08157e584c54e9fce093f1a5a871a8763a",
    urls = ["http://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.2.0/protoc-gen-grpc-java-1.2.0-osx-x86_64.exe"],
)

http_archive(
    name = "com_google_protobuf",
    sha256 = "9510dd2afc29e7245e9e884336f848c8a6600a14ae726adb6befdb4f786f0be2",
    strip_prefix = "protobuf-%s" % PROTOBUF_VERSION,
    url = "https://github.com/google/protobuf/archive/v%s.zip" % PROTOBUF_VERSION,
)

bind(
    name = "protocol_compiler",
    actual = "@com_google_protobuf//:protoc",
)

STACKB_RULES_PROTO_VERSION = "8afa882b3dff5fec93b22519d34d0099083a7ce2"

http_archive(
    name = "build_stack_rules_proto",
    sha256 = "0d88313ba32c0042c2633c3cbdd187afb0c3c9468b978f6eb4919ac6e535f029",
    strip_prefix = "rules_proto-%s" % STACKB_RULES_PROTO_VERSION,
    url = "https://github.com/stackb/rules_proto/archive/%s.tar.gz" % STACKB_RULES_PROTO_VERSION,
)

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "9d23d9fec84e24bd3962f5ef9d1fd61ce939d3f649a22bcab0f19e8167fae8ef",
    strip_prefix = "grpc-java-%s" % GRPC_JAVA_VERSION,
    url = "https://github.com/grpc/grpc-java/archive/v%s.zip" % GRPC_JAVA_VERSION,
)

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories(omit_com_google_protobuf = True)

"""
Import Google API types
"""

GOOGLE_API_TYPES_VERSION = "10049e8ea946100bb7da66f63b0ecd1a345e8760"

http_archive(
    name = "com_google_googleapis",
    sha256 = "ddd2cd7b6b310028b8ba08057d2990ced6f78c35fdf5083ff142704f1c2c5e49",
    strip_prefix = "googleapis-%s" % GOOGLE_API_TYPES_VERSION,
    url = "https://github.com/googleapis/googleapis/archive/%s.zip" % GOOGLE_API_TYPES_VERSION,
)

load("@com_google_googleapis//:repository_rules.bzl", "switched_rules_by_language")

switched_rules_by_language(
    name = "com_google_googleapis_imports",
    grpc = True,
    java = True,
)
