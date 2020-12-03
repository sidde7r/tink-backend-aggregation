# Use the new Skylark version of git_repository. This uses the system's native
# git client which supports fancy key formats and key passphrases.
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository", "new_git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")

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

versions.check("3.0.0", "3.4.1")

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

new_git_repository(
    name = "dropwizard_jersey",
    build_file = "//third_party:dropwizard_jersey.BUILD",
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
    commit = "2110f167bbc6c173f05c92bf4777d013ad31869c",
    remote = "git@github.com:tink-ab/tink-grpc.git",
    shallow_since = "1575523605 +0000",
)

# Go rules

http_archive(
    name = "io_bazel_rules_go",
    sha256 = "e6a6c016b0663e06fa5fccf1cd8152eab8aa8180c583ec20c872f4f9953a7ac5",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_go/releases/download/v0.22.1/rules_go-v0.22.1.tar.gz",
        "https://github.com/bazelbuild/rules_go/releases/download/v0.22.1/rules_go-v0.22.1.tar.gz",
    ],
)

load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")

go_rules_dependencies()

go_register_toolchains()

# End Go rules

# Gazelle for generating Go packages

http_archive(
    name = "bazel_gazelle",
    sha256 = "7fc87f4170011201b1690326e8c16c5d802836e3a0d617d8f75c3af2b23180c4",
    urls = [
        "https://storage.googleapis.com/bazel-mirror/github.com/bazelbuild/bazel-gazelle/releases/download/0.18.2/bazel-gazelle-0.18.2.tar.gz",
        "https://github.com/bazelbuild/bazel-gazelle/releases/download/0.18.2/bazel-gazelle-0.18.2.tar.gz",
    ],
)

load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies")
load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies", "go_repository")

gazelle_dependencies()

go_repository(
    name = "org_golang_google_grpc",
    build_file_proto_mode = "disable",
    importpath = "google.golang.org/grpc",
    sum = "h1:J0UbZOIrCAl+fpTOf8YLs4dJo8L/owV4LYVtAXQoPkw=",
    version = "v1.22.0",
)

go_repository(
    name = "org_golang_x_net",
    importpath = "golang.org/x/net",
    sum = "h1:oWX7TPOiFAMXLq8o0ikBYfCJVlRHBcsciT5bXOrH628=",
    version = "v0.0.0-20190311183353-d8887717615a",
)

go_repository(
    name = "org_golang_x_text",
    importpath = "golang.org/x/text",
    sum = "h1:g61tztE5qeGQ89tm6NTjjM9VPIm088od1l6aSorWRWg=",
    version = "v0.3.0",
)

go_repository(
    name = "org_golang_google_genproto",
    importpath = "google.golang.org/genproto",
)

# End Gazelle

git_repository(
    name = "tink_backend",
    commit = "55072f227a75c6b365891d86c71442c9791b7b3d",
    remote = "git@github.com:tink-ab/tink-backend.git",
    shallow_since = "1601479333 +0000",
)

# To be used only by //src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework
git_repository(
    name = "tink_backend_for_agents_framework",
    commit = "aada680958ac940e5ca82205440e7f0bc976f8f4",
    remote = "git@github.com:tink-ab/tink-backend.git",
    shallow_since = "1595000000 +0000",
)

# Docker dependencies
http_archive(
    name = "io_bazel_rules_docker",
    sha256 = "1698624e878b0607052ae6131aa216d45ebb63871ec497f26c67455b34119c80",
    strip_prefix = "rules_docker-0.15.0",
    urls = ["https://github.com/bazelbuild/rules_docker/releases/download/v0.15.0/rules_docker-v0.15.0.tar.gz"],
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
    name = "openjdk_jre8",
    registry = "gcr.io",
    repository = "tink-containers/openjdk-8-jre",
    tag = "8",
)

container_pull(
    name = "openjdk_jdk8",
    registry = "gcr.io",
    repository = "tink-containers/openjdk-8-jdk",
    tag = "8",
)

RULES_CODEOWNERS_VERSION = "b08239b88705a5d2d9c613afc6a70ece73e32cad"

RULES_CODEOWNERS_SHA = "cbb9fdfb30ac57678c8bbf6a07b2ccf7993799dbdf6cb3c8549499bb23d09235"

http_archive(
    name = "rules_codeowners",
    sha256 = RULES_CODEOWNERS_SHA,
    strip_prefix = "rules_codeowners-%s" % RULES_CODEOWNERS_VERSION,
    url = "https://github.com/zegl/rules_codeowners/archive/%s.zip" % RULES_CODEOWNERS_VERSION,
)

# You *must* import the Go rules before setting up the go_image rules.
load(
    "@io_bazel_rules_docker//go:image.bzl",
    _go_image_repos = "repositories",
)

_go_image_repos()

## External dependencies
# External repositories that are not under Tink control. These should *always*
# be locked to something stable (a commit, not a branch for example) and have
# a checksum to prevent tampering by the remote end.

# libm4ri library, needed by https://github.com/tink-ab/tink-backend-aggregation/tree/master/tools/libkbc_wbaes_src
http_file(
    name = "libm4ri_dev",
    downloaded_file_path = "libm4ri-dev_20140914-2+b1_amd64.deb",
    sha256 = "040b81df10945380424d8874d38c062f45a5fee6886ae8e6963c87393ba84cd9",
    urls = ["https://mirror.deepines.com/deepin/pool/main/libm/libm4ri/libm4ri-dev_20140914-2%2Bb1_amd64.deb",
            "http://ftp.br.debian.org/debian/pool/main/libm/libm4ri/libm4ri-dev_20140914-2+b1_amd64.deb"],
)

http_file(
    name = "libm4ri_0.0.20140914",
    downloaded_file_path = "libm4ri-0.0.20140914_20140914-2+b1_amd64.deb",
    sha256 = "c2f38d51730b6e9a73e2f4d2e0edfadf647a9889da9d06a15abca07d3eccc6f1",
    urls = ["https://mirror.deepines.com/deepin/pool/main/libm/libm4ri/libm4ri-0.0.20140914_20140914-2%2Bb1_amd64.deb",
            "http://ftp.br.debian.org/debian/pool/main/libm/libm4ri/libm4ri-0.0.20140914_20140914-2+b1_amd64.deb"],
)

# libtesseract, needed by some RE agents
http_file(
    name = "libtesseract-dev_4.0.0-2",
    downloaded_file_path = "libtesseract-dev_4.0.0-2_amd64.deb",
    sha256 = "1e509dc182e4e7a6de19ce2aa8b2c8b89006edd4497aa27f7d66db3f3a6b8f4b",
    urls = ["http://ftp.se.debian.org/debian/pool/main/t/tesseract/libtesseract-dev_4.0.0-2_amd64.deb"],
)

http_file(
    name = "libtesseract4_4.0.0-2",
    downloaded_file_path = "libtesseract4_4.0.0-2_amd64.deb",
    sha256 = "8e96d37eceff951c9e89f328577cb177faf6813bbd76a8c4a7deede72f73a680",
    urls = ["http://ftp.se.debian.org/debian/pool/main/t/tesseract/libtesseract4_4.0.0-2_amd64.deb"],
)

http_file(
    name = "liblept5_1.74.1-1",
    downloaded_file_path = "liblept5_1.74.1-1_amd64.deb",
    sha256 = "64d9ae6d3101b5a85118109178df3575d92ca25ff0b273b732280cb9580fe416",
    urls = ["http://ftp.acc.umu.se/debian/pool/main/l/leptonlib/liblept5_1.74.1-1_amd64.deb"],
)

http_file(
    name = "libgomp1_8.3.0-6",
    downloaded_file_path = "libgomp1_8.3.0-6_amd64.deb",
    sha256 = "909fcd28491d7ebecf44ee2e8d0269b600271b0b6d236b19f2c0469cde162d21",
    urls = ["http://ftp.se.debian.org/debian/pool/main/g/gcc-8/libgomp1_8.3.0-6_amd64.deb"],
)

http_file(
    name = "libwebp6_0.6.1-2",
    downloaded_file_path = "libwebp6_0.6.1-2_amd64.deb",
    sha256 = "7d9cb5e08149327731e84380e454a56f148c517ec2ecad30900c6837d0b1b76a",
    urls = ["http://ftp.se.debian.org/debian/pool/main/libw/libwebp/libwebp6_0.6.1-2_amd64.deb"],
)

http_file(
    name = "libopenjp2-7_2.3.0-2",
    downloaded_file_path = "libopenjp2-7_2.3.0-2+deb10u1_amd64.deb",
    sha256 = "be133e48ac8894d4824b6106fe361a1b46acbcef8232b3b98dc04455da90e02a",
    urls = ["http://ftp.se.debian.org/debian/pool/main/o/openjpeg2/libopenjp2-7_2.3.0-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libc6_2.28-10",
    downloaded_file_path = "libc6_2.28-10_amd64.deb",
    sha256 = "6f703e27185f594f8633159d00180ea1df12d84f152261b6e88af75667195a79",
    urls = ["http://ftp.se.debian.org/debian/pool/main/g/glibc/libc6_2.28-10_amd64.deb"],
)

# proto_library rules implicitly depend on @com_google_protobuf//:protoc,
# which is the proto-compiler.
PROTOBUF_VERSION = "3.9.0"

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty
GRPC_JAVA_VERSION = "1.29.0"

IO_NETTY_VERSION = "4.1.48.Final"

IO_NETTY_BORINGSSL_VERSION = "2.0.30.Final"

GRPC_JAVA_NANO_VERSION = "1.21.1"

ECLIPSE_JETTY_VERSION = "9.0.7.v20131107"

ALT_ECLIPSE_JETTY_VERSION = "9.2.13.v20150730"

# rules_proto will not be builtin in to Bazel in v1.0 and later
# prepare us for that, and use the out-ot-repo version
http_archive(
    name = "rules_proto",
    sha256 = "4d421d51f9ecfe9bf96ab23b55c6f2b809cbaf0eea24952683e397decfbd0dd0",
    strip_prefix = "rules_proto-f6b8d89b90a7956f6782a4a3609b2f0eee3ce965",
    urls = [
        "https://github.com/bazelbuild/rules_proto/archive/f6b8d89b90a7956f6782a4a3609b2f0eee3ce965.tar.gz",
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

bind(
    name = "cpsuite",
    actual = "@aggregation//:io_takari_junit_takari_cpsuite",
)

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

OPENCENSUS_VERSION = "0.21.0"

# Dependency shading
# https://softwareengineering.stackexchange.com/questions/297276/what-is-a-shaded-java-dependency
http_archive(
    name = "com_github_johnynek_bazel_jar_jar",
    sha256 = "97c5f862482a05f385bd8f9d28a9bbf684b0cf3fae93112ee96f3fb04d34b193",
    strip_prefix = "bazel_jar_jar-171f268569384c57c19474b04aebe574d85fde0d",
    url = "https://github.com/johnynek/bazel_jar_jar/archive/171f268569384c57c19474b04aebe574d85fde0d.tar.gz",
)

load("@com_github_johnynek_bazel_jar_jar//:jar_jar.bzl", "jar_jar_repositories")

jar_jar_repositories()

# Order matters: the first one listed is the default repo to fetch from
MAVEN_REPOS = [
    "https://repo1.maven.org/maven2",
    "https://jcenter.bintray.com",
    "https://maven.google.com",
]

jvm_maven_import_external(
    name = "org_xerial_snappy_snappy_java",
    artifact = "org.xerial.snappy:snappy-java:1.1.7",
    artifact_sha256 = "c4e14c31d49e4301a9643ed60112136797b6244316bbe44ce190ac37ed8dfdaf",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "com_sproutsocial_nsqj_j",
    artifact = "com.sproutsocial:nsq-j:0.9.1",
    artifact_sha256 = "bbb5cfffb7af71329f188172b02d7fc6d0a1f199fce3bf09ce189728355e9646",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "com_google_http_client_google_http_client",
    artifact = "com.google.http-client:google-http-client:1.17.0-rc",
    artifact_sha256 = "c8e85f0d4882c012aeae02c954269eadb4b658b8db6e485b3d8241944b68ee0e",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "com_google_j2objc_j2objc_annotations",
    artifact = "com.google.j2objc:j2objc-annotations:1.1",
    artifact_sha256 = "2994a7eb78f2710bd3d3bfb639b2c94e219cedac0d4d084d516e78c16dddecf6",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "javax_annotation_javax_annotation_api",  # Do not use, but do not remove
    artifact = "javax.annotation:javax.annotation-api:1.3.2",
    artifact_sha256 = "e04ba5195bcd555dc95650f7cc614d151e4bcd52d29a10b8aa2197f3ab89ab9b",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "com_googlecode_libphonenumber_libphonenumber",
    artifact = "com.googlecode.libphonenumber:libphonenumber:5.7",
    artifact_sha256 = "a59f64309aa84a9ba4894b7772804c20415ffc4dfbe0a39f5b28ffa99c655b22",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "org_slf4j_slf4j_api",  # Do not use, but do not remove
    artifact = "org.slf4j:slf4j-api:1.7.22",
    artifact_sha256 = "3a4cd4969015f3beb4b5b4d81dbafc01765fb60b8a439955ca64d8476fef553e",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "org_slf4j_jul_to_slf4j",  # Do not use, but do not remove
    artifact = "org.slf4j:jul-to-slf4j:1.7.6",
    artifact_sha256 = "b71451847d2643a1bfe83df69fbf696860fe2689b95c9bd96188fb7808444f64",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "commons_codec_commons_codec",  # Do not use, but do not remove
    artifact = "commons-codec:commons-codec:1.11",
    artifact_sha256 = "e599d5318e97aa48f42136a2927e6dfa4e8881dff0e6c8e3109ddbbff51d7b7d",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "org_jboss_logging_jboss_logging",  # Do not use, but do not remove
    artifact = "org.jboss.logging:jboss-logging:3.1.3.GA",
    artifact_sha256 = "6813931fe607469989f76a73a22515d2489dcd8b6be9fc147093a9cec995f822",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "org_slf4j_jcl_over_slf4j",  # Do not use, but do not remove
    artifact = "org.slf4j:jcl-over-slf4j:1.7.6",
    artifact_sha256 = "d52f5e9a861f4e124ec43d711b566b4c2afe6e0709b490497fb9ca33e1ca0ba5",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "commons_lang_commons_lang",  # Do not use, but do not remove
    artifact = "commons-lang:commons-lang:2.6",
    artifact_sha256 = "50f11b09f877c294d56f24463f47d28f929cf5044f648661c0f0cfbae9a2f49c",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "com_yubico_yubico_validation_client2",  # Do not use, but do not remove
    artifact = "com.yubico:yubico-validation-client2:2.0.1",
    artifact_sha256 = "b420337688e004a989a5c4c76cdce5f4a3cb38ad1d33648584aaa2a18a81b973",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "org_javassist_javassist",  # Do not use, but do not remove
    artifact = "org.javassist:javassist:3.26.0-GA",
    artifact_sha256 = "ca5625874ff0a34f2422173a511b33c225218c146a3c961b18940efff430462d",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "org_hibernate_hibernate_validator",  # Do not use, but do not remove
    artifact = "org.hibernate:hibernate-validator:5.1.1.Final",
    artifact_sha256 = "5a0cfd8f7536f5c3d95de73d023501859c4e035597aa8d80461ecd42168124f5",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "commons_logging_commons_logging",  # Do not use, but do not remove
    artifact = "commons-logging:commons-logging:1.2",
    artifact_sha256 = "daddea1ea0be0f56978ab3006b8ac92834afeefbd9b7e4e6316fca57df0fa636",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "com_google_guava_guava",  # Do not use, but do not remove
    artifact = "com.google.guava:guava:25.1-jre",
    artifact_sha256 = "6db0c3a244c397429c2e362ea2837c3622d5b68bb95105d37c21c36e5bc70abf",
    fetch_sources = True,
    server_urls = MAVEN_REPOS,
    srcjar_sha256 = "b7ffb578b2bd6445c958356e308d1c46c9ea6fb868fc9444bc8bda3a41875a1b",
)

jvm_maven_import_external(
    name = "com_lambdaworks_scrypt",  # Do not use, but do not remove
    artifact = "com.lambdaworks:scrypt:1.3.2",
    artifact_sha256 = "56c73991d535bce0521c8e5ba4204a21cdccf38081f17f19781f83165829b3ac",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "io_grpc_grpc_protobuf_lite",  # Do not use, but do not remove
    artifact = "io.grpc:grpc-protobuf-lite:%s" % GRPC_JAVA_VERSION,
    artifact_sha256 = "ae4bbcd9bf7ad4856660807d8cba7ef4ff428f0b615bf663ba308d9a76bcab3c",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "io_grpc_grpc_context",
    artifact = "io.grpc:grpc-context:%s" % GRPC_JAVA_VERSION,
    artifact_sha256 = "41426f8fa5b5ff6e8cf5d6a7a6e7b1175350bc8c8e11f352e0622e00f99c4a02",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "io_grpc_grpc_protobuf_nano",  # Do not use, but do not remove
    artifact = "io.grpc:grpc-protobuf-nano:%s" % GRPC_JAVA_NANO_VERSION,
    artifact_sha256 = "382ced635da516db0c26db23daeb50c4db5fa37acd0b6469ed4755a641baa022",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "io_grpc_grpc_core",  # Do not use, but do not remove
    artifact = "io.grpc:grpc-core:%s" % GRPC_JAVA_VERSION,
    artifact_sha256 = "d45e3ba310cf6a5d8170bcc500507977505614583c341d03c7d91658e49cf028",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "io_grpc_grpc_api",  # Do not use, but do not remove
    artifact = "io.grpc:grpc-api:%s" % GRPC_JAVA_VERSION,
    artifact_sha256 = "4837824acdd8d576d7d31a862e7391c38a1824cd2224daa68999377fdff9ae3f",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "io_grpc_grpc_netty",  # Do not use, but do not remove
    artifact = "io.grpc:grpc-netty:%s" % GRPC_JAVA_VERSION,
    artifact_sha256 = "e959abda6b0cde0104a4a9398f867f15eefbad81ba64a2174eca1616767f9063",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "io_grpc_grpc_services",  # Do not use, but do not remove
    artifact = "io.grpc:grpc-services:%s" % GRPC_JAVA_VERSION,
    artifact_sha256 = "6bea2f0ec35d3071a12fccc640ca7450f1cd2ce66574456e8deec21f79464681",
    server_urls = MAVEN_REPOS,
)

http_archive(
    name = "grpc_ecosystem_grpc_gateway",
    sha256 = "b14c0ad883933705bfaeffcc695f07bf1e435e7f27e5999d164eb22ced105b3d",
    strip_prefix = "grpc-gateway-1.12.2",
    url = "https://github.com/grpc-ecosystem/grpc-gateway/archive/v1.12.2.zip",
)

jvm_maven_import_external(
    name = "com_google_instrumentation_instrumentation_api",  # Do not use, but do not remove
    artifact = "com.google.instrumentation:instrumentation-api:0.4.3",
    artifact_sha256 = "9502d5622fea56e5b3fbe4a5263ad3bfd93487869813304c36831e1cb1d88bd5",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "com_google_api_grpc_proto_google_common_protos",
    artifact = "com.google.api.grpc:proto-google-common-protos:0.1.9",
    artifact_sha256 = "bf76dcb173f6c6083ccf452f093b53500621e701645df47671c47043c7b5491f",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "com_google_protobuf_protobuf_java",  # Do not use, but do not remove
    artifact = "com.google.protobuf:protobuf-java:3.5.1",
    artifact_sha256 = "b5e2d91812d183c9f053ffeebcbcda034d4de6679521940a19064714966c2cd4",
    server_urls = MAVEN_REPOS,
)

# Used by java_grpc_library

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "446ad7a2e85bbd05406dbf95232c7c49ed90de83b3b60cb2048b0c4c9f254d29",
    strip_prefix = "grpc-java-%s" % GRPC_JAVA_VERSION,
    urls = [
        "https://github.com/grpc/grpc-java/archive/v%s.zip" % GRPC_JAVA_VERSION,
    ],
)

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories()

RULES_JVM_EXTERNAL_TAG = "3.2"

RULES_JVM_EXTERNAL_SHA = "82262ff4223c5fda6fb7ff8bd63db8131b51b413d26eb49e3131037e79e324af"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@tink_backend//third_party/maven:deps.bzl", "maven_deps")

maven_deps(
    "//third_party/maven:maven_install.json",
    IO_NETTY_VERSION,
    IO_NETTY_BORINGSSL_VERSION,
    artifact_versions = {"javax.validation:validation-api": "2.0.1.Final"},
)

load("@maven//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

# Agents Platform

load("@tink_backend_for_agents_framework//src/agents-platform:deps.bzl", "agent_platform_deps", "lombok_deps")

lombok_deps("@tink_backend_for_agents_framework//src/agents-platform:lombok_maven_install.json")

load("@lombok_maven//:defs.bzl", pin_lombok = "pinned_maven_install")

pin_lombok()

agent_platform_deps("@tink_backend_for_agents_framework//src/agents-platform:agent_platform_maven_install.json", GRPC_JAVA_VERSION)

load("@agents_platform_maven//:defs.bzl", pin_agent_platform = "pinned_maven_install")

pin_agent_platform()

load("@tink_backend_for_agents_framework//src/libraries/cryptography:deps.bzl", "cryptography_lib_deps")

cryptography_lib_deps("@tink_backend_for_agents_framework//src/libraries/cryptography:cryptography_lib_install.json")

# This aims become the singular place for specifying the full collection of direct and transitive
# dependencies of the aggregation service monolith jar. All aggregation code -- including agent code
# -- shall ideally depend on artifacts provided by this maven_install and nothing else.
#
# Rules of thumb:
# 1. A Bazel target should not depend on artifacts from multiple maven_installs.
# 2. If two applications have no dependencies on each other, give them separate maven_installs.
# 3. Consequently, whitebox tests should depend on the same maven_install as the code under test.
#
# As long as these rules are followed, we will not be in danger of mixing different versions of
# transitive dependencies in Bazel targets, and will be possible to upgrade dependencies flexibly.
#
# At the time of writing, aggregation production code still depends on artifacts via the maven_jar
# rules in WORKSPACE. This is done either by referring to them directly using the "@artifact//jar"
# syntax, or indirectly via the third_party java_library rules using the "//third_party:artifact"
# syntax. Either way, any such dependency is to be replaced with a direct dependency on an artifact
# in the list below. This is not a trivial task, and should be done in an incremental fashion. The
# recommendation is to make one commit for adding the desired artifact to the list below + pinning
# the JSON file. And then make a second commit that replaces all direct or indirect usages of the
# corresponding maven_jar (if any) with a direct dependency on the newly added artifact.
#
# Adding a new artifact to the list below is a delicate process, and is not to be taken lightly.
# It may be necessary to replace a few dependencies on other maven_jar rules before you are in a
# safe position to add the artifact you desire. It may also be necessary to adjust the version of
# a few existing artifacts to reconcile the versions of the artifacts specified here vs. the
# artifacts that your desired artifact depends on transitively.
#
# The rule of thumb is to always add a new artifact in a way so that only two entries are added to
# aggregation_install.json -- one for the jar itself and one for its sources. Furthermore, in this
# JSON file, conflict_resolution should ideally remain as empty as possible. If it is non-empty,
# this means that there exists at least one artifact, A, that has to use an incompatible version of
# another artifact, B. This means that there is no guarantee that artifact A will be functioning
# correctly in production. It is possible that certain pieces of code in A will crash with
# NoSuchMethodError or the like.
maven_install(
    name = "aggregation",
    artifacts = [
        "antlr:antlr:2.7.6",
        "aopalliance:aopalliance:1.0",
        "asm:asm:3.3.1",
        "c3p0:c3p0:0.9.1.1",
        "ch.qos.logback.contrib:logback-jackson:0.1.5",
        "ch.qos.logback.contrib:logback-json-classic:0.1.5",
        "ch.qos.logback.contrib:logback-json-core:0.1.5",
        "ch.qos.logback:logback-classic:1.1.11",
        "ch.qos.logback:logback-core:1.1.11",
        "com.codahale.metrics:metrics-annotation:3.0.2",
        "com.codahale.metrics:metrics-core:3.0.2",
        "com.codahale.metrics:metrics-healthchecks:3.0.2",
        "com.codahale.metrics:metrics-httpclient:3.0.2",
        "com.codahale.metrics:metrics-jersey:3.0.2",
        "com.codahale.metrics:metrics-jetty9:3.0.2",
        "com.codahale.metrics:metrics-json:3.0.2",
        "com.codahale.metrics:metrics-jvm:3.0.2",
        "com.codahale.metrics:metrics-logback:3.0.2",
        "com.codahale.metrics:metrics-servlets:3.0.2",
        "com.fasterxml.jackson.core:jackson-annotations:2.9.9",
        "com.fasterxml.jackson.core:jackson-core:2.9.9",
        "com.fasterxml.jackson.core:jackson-databind:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.9",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-base:2.9.9",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.9.9",
        "com.fasterxml.jackson.module:jackson-module-afterburner:2.9.9",
        "com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.9.9",
        "com.fasterxml.jackson.module:jackson-module-jsonSchema:2.9.9",
        "com.kjetland:mbknor-jackson-jsonschema_2.12:1.0.34",
        "com.fasterxml.uuid:java-uuid-generator:3.1.5",
        "com.fasterxml:classmate:1.0.0",
        "com.flipkart.zjsonpatch:zjsonpatch:0.2.1",
        "com.github.jai-imageio:jai-imageio-core:1.4.0",
        "com.github.javafaker:javafaker:1.0.2",
        "com.github.rholder:guava-retrying:2.0.0",
        "com.github.stephenc.jcip:jcip-annotations:1.0-1",
        "com.google.api.grpc:proto-google-common-protos:0.1.9",
        "com.google.code.findbugs:jsr305:3.0.2",
        "com.google.code.gson:gson:2.8.2",
        "com.google.errorprone:error_prone_annotations:2.3.3",
        "com.google.guava:guava:25.1-jre",
        "com.google.http-client:google-http-client:1.17.0-rc",
        "com.google.inject.extensions:guice-assistedinject:4.2.2",
        "com.google.inject.extensions:guice-grapher:4.2.2",
        "com.google.inject.extensions:guice-multibindings:4.2.2",
        "com.google.inject:guice:4.2.2",
        "com.google.instrumentation:instrumentation-api:0.4.3",
        "com.google.protobuf:protobuf-java-util:3.5.1",
        "com.google.protobuf:protobuf-java:3.5.1",
        "com.google.zxing:core:3.3.3",
        "com.google.zxing:javase:3.3.3",
        "com.googlecode.gettext-commons:gettext-commons:0.9.8",
        "com.jayway.jsonpath:json-path:2.4.0",
        "com.jcraft:jzlib:1.1.3",
        "com.kjetland:mbknor-jackson-jsonschema_2.12:1.0.34",
        "com.lambdaworks:scrypt:1.3.2",
        "com.netflix.governator:governator-api:1.17.2",
        "com.netflix.governator:governator-core:1.17.2",
        "com.netflix.governator:governator:1.17.2",
        "com.nimbusds:srp6a:2.0.2",
        "com.opsgenie.integration:sdk-shaded:jar:2.8.2",
        "com.sun.jersey.contribs:jersey-apache-client4:1.18.1",
        "com.sun.jersey:jersey-client:1.18.1",
        "com.sun.jersey:jersey-core:1.18.1",
        "com.sun.jersey:jersey-server:1.18.1",
        "com.sun.jersey:jersey-servlet:1.18.1",
        "com.yubico:yubico-validation-client2:2.0.1",
        "commons-cli:commons-cli:1.4",
        "commons-codec:commons-codec:1.11",
        "commons-collections:commons-collections:3.2.2",
        "commons-httpclient:commons-httpclient:3.1",
        "commons-io:commons-io:2.5",
        "commons-lang:commons-lang:2.6",
        "commons-logging:commons-logging:1.2",
        "de.jollyday:jollyday:0.4.7",
        "dom4j:dom4j:1.6.1",
        "eu.geekplace.javapinning:java-pinning-jar:1.0.1",
        "io.dropwizard:dropwizard-configuration:0.7.1",
        "io.dropwizard:dropwizard-jackson:0.7.1",
        "io.dropwizard:dropwizard-jetty:0.7.1",
        "io.dropwizard:dropwizard-lifecycle:0.7.1",
        "io.dropwizard:dropwizard-logging:0.7.1",
        "io.dropwizard:dropwizard-metrics:0.7.1",
        "io.dropwizard:dropwizard-servlets:0.7.1",
        "io.dropwizard:dropwizard-util:0.7.1",
        "io.dropwizard:dropwizard-validation:0.7.1",
        "io.github.classgraph:classgraph:4.8.78",
        "io.github.resilience4j:resilience4j-all:1.6.1",
        "io.grpc:grpc-context:%s" % GRPC_JAVA_VERSION,
        "io.netty:netty-buffer:%s" % IO_NETTY_VERSION,
        "io.netty:netty-codec-dns:%s" % IO_NETTY_VERSION,
        "io.netty:netty-codec-http2:%s" % IO_NETTY_VERSION,
        "io.netty:netty-codec-http:%s" % IO_NETTY_VERSION,
        "io.netty:netty-codec-socks:%s" % IO_NETTY_VERSION,
        "io.netty:netty-codec:%s" % IO_NETTY_VERSION,
        "io.netty:netty-common:%s" % IO_NETTY_VERSION,
        "io.netty:netty-dev-tools:%s" % IO_NETTY_VERSION,
        "io.netty:netty-handler-proxy:%s" % IO_NETTY_VERSION,
        "io.netty:netty-handler:%s" % IO_NETTY_VERSION,
        "io.netty:netty-resolver-dns:%s" % IO_NETTY_VERSION,
        "io.netty:netty-resolver:%s" % IO_NETTY_VERSION,
        "io.netty:netty-tcnative-boringssl-static:%s" % IO_NETTY_BORINGSSL_VERSION,
        "io.netty:netty-transport:%s" % IO_NETTY_VERSION,
        "io.netty:netty:3.10.5.Final",
        "io.opencensus:opencensus-api:%s" % OPENCENSUS_VERSION,
        "io.perfmark:perfmark-api:0.17.0",
        "io.prometheus:simpleclient:0.5.0",
        "io.prometheus:simpleclient_common:0.5.0",
        "io.prometheus:simpleclient_hotspot:0.5.0",
        "io.prometheus:simpleclient_httpserver:0.5.0",
        "io.prometheus:simpleclient_pushgateway:0.5.0",
        "io.reactivex.rxjava3:rxjava:3.0.0-RC4",
        "io.swagger:swagger-annotations:1.5.9",
        "io.takari.junit:takari-cpsuite:1.2.7",
        "io.vavr:vavr-jackson:0.10.2",
        "io.vavr:vavr-match:0.10.2",
        "io.vavr:vavr-test:0.10.2",
        "io.vavr:vavr:0.10.2",
        "jakarta.xml.bind:jakarta.xml.bind-api:2.3.3",
        "jakarta.xml.soap:jakarta.xml.soap-api:1.4.1",
        "javax.el:javax.el-api:2.2.5",
        "javax.inject:javax.inject:1",
        "javax.servlet:javax.servlet-api:4.0.1",
        "javax.transaction:jta:1.1",
        "javax.validation:validation-api:2.0.1.Final",
        "javax.xml.bind:jaxb-api:2.2.7",
        "javax.xml.stream:stax-api:1.0-2",
        "jline:jline:0.9.94",
        "joda-time:joda-time:2.9.9",
        "junit:junit:4.12",
        "log4j:log4j:1.2.17",
        "mysql:mysql-connector-java:5.1.42",
        "net.bytebuddy:byte-buddy-agent:1.10.1",
        "net.bytebuddy:byte-buddy:1.10.1",
        "net.jadler:jadler-all:1.3.0",
        "net.java.dev.jna:jna:5.3.1",
        "net.minidev:asm:1.0.2",
        "net.minidev:json-smart:2.3",
        "net.sf.jopt-simple:jopt-simple:4.9",
        "net.sourceforge.argparse4j:argparse4j:0.4.3",
        "net.sourceforge.cssparser:cssparser:0.9.16",
        "net.spy:spymemcached:2.9.1",
        "org.apache.commons:commons-collections4:4.0",
        "org.apache.commons:commons-csv:1.0",
        "org.apache.commons:commons-lang3:3.9",
        "org.apache.commons:commons-math3:3.2",
        "org.apache.commons:commons-text:1.8",
        "org.apache.httpcomponents:httpclient:4.3.4",
        "org.apache.httpcomponents:httpcore:4.3.2",
        "org.apache.logging.log4j:log4j-api:2.11.1",
        "org.apache.logging.log4j:log4j-core:2.11.1",
        "org.apache.mahout.commons:commons-cli:2.0-mahout",
        "org.apache.pdfbox:fontbox:2.0.6",
        "org.apache.pdfbox:pdfbox:2.0.6",
        "org.aspectj:aspectjrt:1.8.2",
        "org.assertj:assertj-core:3.8.0",
        "org.bitbucket.b_c:jose4j:0.6.5",
        "org.bouncycastle:bcpkix-jdk15on:1.59",
        "org.bouncycastle:bcprov-jdk15on:1.59",
        "org.codehaus.jackson:jackson-mapper-asl:1.8.9",
        "org.codehaus.plexus:plexus-utils:3.0.17",
        "org.codehaus.woodstox:stax2-api:4.1",
        "org.eclipse.jetty.orbit:javax.servlet:3.0.0.v201112011016",
        "org.eclipse.jetty:jetty-continuation:%s" % ECLIPSE_JETTY_VERSION,
        "org.eclipse.jetty:jetty-http:%s" % ECLIPSE_JETTY_VERSION,
        "org.eclipse.jetty:jetty-io:%s" % ECLIPSE_JETTY_VERSION,
        "org.eclipse.jetty:jetty-security:%s" % ECLIPSE_JETTY_VERSION,
        "org.eclipse.jetty:jetty-server:%s" % ECLIPSE_JETTY_VERSION,
        "org.eclipse.jetty:jetty-servlet:%s" % ECLIPSE_JETTY_VERSION,
        "org.eclipse.jetty:jetty-servlets:%s" % ECLIPSE_JETTY_VERSION,
        "org.eclipse.jetty:jetty-util:%s" % ECLIPSE_JETTY_VERSION,
        "org.glassfish.web:javax.el:2.2.6",
        "org.hamcrest:hamcrest-core:1.3",
        "org.hamcrest:hamcrest-library:1.3",
        "org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.0.Final",
        "org.hibernate:hibernate-annotations:3.5.4-Final",
        "org.hibernate:hibernate-commons-annotations:3.2.0.Final",
        "org.hibernate:hibernate-core:3.5.4-Final",
        "org.hibernate:hibernate-validator:5.1.1.Final",
        "org.iban4j:iban4j:3.1.0",
        "org.javassist:javassist:3.26.0-GA",
        "org.jboss.logging:jboss-logging:3.1.3.GA",
        "org.json:json:20080701",
        "org.jsoup:jsoup:1.7.2",
        "org.mockito:mockito-core:3.0.0",
        "org.modelmapper:modelmapper:1.1.0",
        "org.mozilla:rhino:1.7R4",
        "org.objenesis:objenesis:2.6",
        "org.ow2.asm:asm:5.0.4",
        "org.pojava:pojava:2.8.1",
        "org.reactivestreams:reactive-streams:1.0.3",
        "org.reflections:reflections:0.9.11",
        "org.slf4j:jcl-over-slf4j:1.7.6",
        "org.slf4j:jul-to-slf4j:1.7.6",
        "org.slf4j:slf4j-api:1.7.30",
        "org.slf4j:slf4j-log4j12:1.7.5",
        "org.slf4j:slf4j-simple:1.7.5",
        "org.w3c.css:sac:1.3",
        "org.xerial.snappy:snappy-java:1.0.5-M2",
        "org.xmlunit:xmlunit-core:2.1.1",
        "org.xmlunit:xmlunit-legacy:2.1.1",
        "org.yaml:snakeyaml:1.23",
        "pl.pragmatists:JUnitParams:1.0.5",
        "software.amazon.ion:ion-java:1.0.2",
        "xerces:xercesImpl:2.11.0",
        "xml-apis:xml-apis:1.4.01",
    ],
    excluded_artifacts = [
        # Keep this list empty please
    ],
    fetch_sources = True,
    generate_compat_repositories = False,  # Tempting, but provided that we depend on tink-backend, let's be explicit in our naming of deps
    maven_install_json = "//third_party:aggregation_install.json",
    repositories = MAVEN_REPOS,
    version_conflict_policy = "default",  # Let's stick to Coursier's algorithm and strive for NO CONFLICTS as far as possible
)

load("@aggregation//:defs.bzl", aggregation_pin = "pinned_maven_install")

aggregation_pin()

# To be moved into the aggregation maven_install eventually
maven_install(
    name = "aggregation_temp",
    artifacts = [
        "com.amazonaws:aws-java-sdk-code-generator:1.11.381",
        "com.amazonaws:aws-java-sdk-kms:1.11.381",
        "com.amazonaws:aws-java-sdk-s3:1.11.381",
        "com.amazonaws:aws-java-sdk-sqs:1.11.381",
        "com.amazonaws:jmespath-java:1.11.381",
        "com.auth0:java-jwt:3.3.0",
        "com.github.tomakehurst:wiremock-standalone:2.27.1",
        "com.google.crypto.tink:tink:1.2.2",
        "com.google.errorprone:javac-shaded:9+181-r4173-1",
        "com.google.googlejavaformat:google-java-format:1.7",
        "com.nimbusds:nimbus-jose-jwt:7.7",
        "com.oracle.substratevm:svm:19.0.0",
        "io.dropwizard:dropwizard-client:0.7.1",
        "io.dropwizard:dropwizard-core:0.7.1",
        "io.grpc:grpc-api:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-auth:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-core:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-netty:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-protobuf-lite:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-protobuf-nano:%s" % GRPC_JAVA_NANO_VERSION,
        "io.grpc:grpc-protobuf:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-services:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-stub:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-testing-proto:1.24.0",
        "io.grpc:grpc-testing:%s" % GRPC_JAVA_VERSION,
        "io.opencensus:opencensus-contrib-grpc-metrics:%s" % OPENCENSUS_VERSION,
        "net.sourceforge.lept4j:lept4j:1.10.0",
        "net.sourceforge.tess4j:tess4j:4.0.2",
        "org.apache.curator:curator-client:4.0.0",
        "org.apache.curator:curator-framework:4.0.1",
        "org.apache.curator:curator-recipes:4.0.0",
        "org.apache.curator:curator-x-discovery:4.0.0",
        "org.apache.zookeeper:zookeeper:3.5.3-beta",
        "org.eclipse.jetty.toolchain.setuid:jetty-setuid-java:1.0.2",
        "org.eclipse.jetty:jetty-webapp:%s" % ALT_ECLIPSE_JETTY_VERSION,
        "org.eclipse.jetty:jetty-xml:%s" % ALT_ECLIPSE_JETTY_VERSION,
        "org.slf4j:slf4j-simple:1.7.27",
        "org.springframework.data:spring-data-commons:1.13.1.RELEASE",
        "org.springframework.data:spring-data-jpa:1.11.1.RELEASE",
        "org.springframework.security:spring-security-core:4.2.3.RELEASE",
        "org.springframework:spring-aop:4.3.7.RELEASE",
        "org.springframework:spring-beans:4.3.7.RELEASE",
        "org.springframework:spring-context:4.3.7.RELEASE",
        "org.springframework:spring-core:4.3.7.RELEASE",
        "org.springframework:spring-expression:4.3.7.RELEASE",
        "org.springframework:spring-jdbc:4.3.7.RELEASE",
        "org.springframework:spring-orm:4.3.7.RELEASE",
        "org.springframework:spring-tx:4.3.7.RELEASE",
        "cglib:cglib:2.2",
        "org.hibernate:hibernate-entitymanager:3.5.4-Final",
    ],
    # Exclude ALL transitive dependencies of the artifacts above for now
    excluded_artifacts = [
        "aopalliance:aopalliance",
        "asm:asm",
        "ch.qos.logback:logback-classic",
        "ch.qos.logback:logback-core",
        "com.amazonaws:aws-java-sdk-core",
        "com.codahale.metrics:metrics-annotation",
        "com.codahale.metrics:metrics-core",
        "com.codahale.metrics:metrics-healthchecks",
        "com.codahale.metrics:metrics-httpclient",
        "com.codahale.metrics:metrics-jersey",
        "com.codahale.metrics:metrics-jetty9",
        "com.codahale.metrics:metrics-json",
        "com.codahale.metrics:metrics-jvm",
        "com.codahale.metrics:metrics-logback",
        "com.codahale.metrics:metrics-servlets",
        "com.fasterxml.jackson.core:jackson-annotations",
        "com.fasterxml.jackson.core:jackson-core",
        "com.fasterxml.jackson.core:jackson-databind",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml",
        "com.fasterxml.jackson.datatype:jackson-datatype-guava",
        "com.fasterxml.jackson.datatype:jackson-datatype-joda",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-base",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider",
        "com.fasterxml.jackson.module:jackson-module-afterburner",
        "com.fasterxml.jackson.module:jackson-module-jaxb-annotations",
        "com.fasterxml:classmate",
        "com.flipkart.zjsonpatch:zjsonpatch",
        "com.github.jai-imageio:jai-imageio-core",
        "com.github.stephenc.jcip:jcip-annotations",
        "com.google.android:annotations",
        "com.google.api-client:google-api-client",
        "com.google.api.grpc:proto-google-common-protos",
        "com.google.apis:google-api-services-cloudkms",
        "com.google.auth:google-auth-library-credentials",
        "com.google.auto.service:auto-service",
        "com.google.auto:auto-common",
        "com.google.code.findbugs:jsr305",
        "com.google.code.gson:gson",
        "com.google.errorprone:error_prone_annotations",
        "com.google.guava:guava",
        "com.google.guava:guava-jdk5",
        "com.google.http-client:google-http-client",
        "com.google.http-client:google-http-client-jackson2",
        "com.google.oauth-client:google-oauth-client",
        "com.google.protobuf.nano:protobuf-javanano",
        "com.google.protobuf:protobuf-java",
        "com.google.protobuf:protobuf-java-util",
        "com.google.protobuf:protobuf-javalite",
        "com.jayway.jsonpath:json-path",
        "com.lowagie:itext",
        "com.oracle.substratevm:objectfile",
        "com.oracle.substratevm:pointsto",
        "com.oracle.substratevm:svm-hosted-native-darwin-amd64",
        "com.oracle.substratevm:svm-hosted-native-linux-amd64",
        "com.oracle.substratevm:svm-hosted-native-windows-amd64",
        "com.sun.jersey.contribs:jersey-apache-client4",
        "com.sun.jersey:jersey-client",
        "com.sun.jersey:jersey-core",
        "com.sun.jersey:jersey-server",
        "com.sun.jersey:jersey-servlet",
        "commons-beanutils:commons-beanutils",
        "commons-cli:commons-cli",
        "commons-codec:commons-codec",
        "commons-collections:commons-collections",
        "commons-io:commons-io",
        "commons-lang:commons-lang",
        "commons-logging:commons-logging",
        "io.dropwizard:dropwizard-configuration",
        "io.dropwizard:dropwizard-jackson",
        "io.dropwizard:dropwizard-jersey",
        "io.dropwizard:dropwizard-jetty",
        "io.dropwizard:dropwizard-lifecycle",
        "io.dropwizard:dropwizard-logging",
        "io.dropwizard:dropwizard-metrics",
        "io.dropwizard:dropwizard-servlets",
        "io.dropwizard:dropwizard-util",
        "io.dropwizard:dropwizard-validation",
        "io.grpc:grpc-context",
        "io.netty:netty",
        "io.netty:netty-buffer",
        "io.netty:netty-codec",
        "io.netty:netty-codec-http",
        "io.netty:netty-codec-http2",
        "io.netty:netty-codec-socks",
        "io.netty:netty-common",
        "io.netty:netty-handler",
        "io.netty:netty-handler-proxy",
        "io.netty:netty-resolver",
        "io.netty:netty-transport",
        "io.opencensus:opencensus-api",
        "io.perfmark:perfmark-api",
        "javax.servlet:javax.servlet-api",
        "javax.validation:validation-api",
        "joda-time:joda-time",
        "junit:junit",
        "log4j:log4j",
        "net.java.dev.jna:jna",
        "net.minidev:asm",
        "net.minidev:json-smart",
        "net.sf.jopt-simple:jopt-simple",
        "net.sourceforge.argparse4j:argparse4j",
        "org.apache.commons:commons-collections4",
        "org.apache.commons:commons-lang3",
        "org.apache.httpcomponents:httpclient",
        "org.apache.httpcomponents:httpcore",
        "org.apache.pdfbox:fontbox",
        "org.apache.pdfbox:jbig2-imageio",
        "org.apache.pdfbox:pdfbox",
        "org.apache.pdfbox:pdfbox-debugger",
        "org.apache.pdfbox:pdfbox-tools",
        "org.apache.xmlgraphics:xmlgraphics-commons",
        "org.aspectj:aspectjrt",
        "org.codehaus.jackson:jackson-core-asl",
        "org.codehaus.jackson:jackson-mapper-asl",
        "org.codehaus.mojo:animal-sniffer-annotations",
        "org.eclipse.core:commands",
        "org.eclipse.core:org.eclipse.core.commands",
        "org.eclipse.core:org.eclipse.core.contenttype",
        "org.eclipse.core:org.eclipse.core.expressions",
        "org.eclipse.core:org.eclipse.core.filesystem",
        "org.eclipse.core:org.eclipse.core.jobs",
        "org.eclipse.core:org.eclipse.core.resources",
        "org.eclipse.core:org.eclipse.core.runtime",
        "org.eclipse.equinox:common",
        "org.eclipse.equinox:org.eclipse.equinox.app",
        "org.eclipse.equinox:org.eclipse.equinox.common",
        "org.eclipse.equinox:org.eclipse.equinox.preferences",
        "org.eclipse.equinox:org.eclipse.equinox.registry",
        "org.eclipse.jdt:org.eclipse.jdt.core",
        "org.eclipse.jetty.orbit:javax.servlet",
        "org.eclipse.jetty:jetty-continuation",
        "org.eclipse.jetty:jetty-http",
        "org.eclipse.jetty:jetty-io",
        "org.eclipse.jetty:jetty-security",
        "org.eclipse.jetty:jetty-server",
        "org.eclipse.jetty:jetty-servlet",
        "org.eclipse.jetty:jetty-servlets",
        "org.eclipse.jetty:jetty-util",
        "org.eclipse.jetty:jetty-webapp",
        "org.eclipse.jetty:jetty-webapp",
        "org.eclipse.jetty:jetty-xml",
        "org.eclipse.jetty:jetty-xml",
        "org.eclipse.osgi:org.eclipse.osgi",
        "org.eclipse.text:org.eclipse.text",
        "org.eclipse:text",
        "org.freemarker:freemarker",
        "org.ghost4j:ghost4j",
        "org.glassfish.web:javax.el",
        "org.graalvm.compiler:compiler",
        "org.graalvm.sdk:graal-sdk",
        "org.graalvm.truffle:truffle-api",
        "org.graalvm.truffle:truffle-nfi",
        "org.hamcrest:hamcrest-core",
        "org.hibernate:hibernate-validator",
        "org.jboss.logging:jboss-logging",
        "org.jboss:jboss-vfs",
        "org.json:json",
        "org.slf4j:jcl-over-slf4j",
        "org.slf4j:jul-to-slf4j",
        "org.slf4j:log4j-over-slf4j",
        "org.slf4j:slf4j-api",
        "org.slf4j:slf4j-log4j12",
        "org.springframework:spring-expression",
        "org.xmlunit:xmlunit-core",
        "org.xmlunit:xmlunit-legacy",
        "software.amazon.ion:ion-java",
        "commons-collections:commons-collections",
        "dom4j:dom4j",
        "javassist:javassist",
        "javax.transaction:jta",
        "org.slf4j:slf4j-api",
        "org.hibernate.javax.persistence:hibernate-jpa-2.0-api",
        "org.hibernate:hibernate-annotations",
        "org.hibernate:hibernate-commons-annotations",
        "org.hibernate:hibernate-core",
        "antlr:antlr",
        "asm:asm",
        "xml-apis:xml-apis",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party:aggregation_temp_install.json",
    repositories = MAVEN_REPOS,
    version_conflict_policy = "pinned",
)

load("@aggregation_temp//:defs.bzl", aggregation_temp_pin = "pinned_maven_install")

aggregation_temp_pin()

SPRING_FRAMEWORK_VERSION = "5.1.5.RELEASE"

SPRING_BOOT_VERSION = "2.1.3.RELEASE"

maven_install(
    name = "system_tests",
    artifacts = [
        "com.fasterxml.jackson.core:jackson-databind:2.9.9",
        "com.google.guava:guava:23.1-jre",
        "commons-io:commons-io:2.5",
        "org.apache.httpcomponents:httpclient:4.5.10",
        "org.assertj:assertj-core:2.2.0",
        "org.hamcrest:hamcrest-core:1.3",
        "org.hamcrest:hamcrest-library:1.3",
        "org.springframework.boot:spring-boot-test:%s" % SPRING_BOOT_VERSION,
        "org.springframework:spring-aop:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-beans:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-context:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-expression:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-test:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-web:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-webmvc:%s" % SPRING_FRAMEWORK_VERSION,
        "org.testcontainers:testcontainers:1.15.0-rc2",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party/system_tests:system_tests_install.json",
    repositories = MAVEN_REPOS,
)

load("@system_tests//:defs.bzl", system_tests_pin = "pinned_maven_install")

system_tests_pin()

# Use via //third_party/jetty_server9
maven_install(
    name = "jetty_server9",
    artifacts = [
        "org.eclipse.jetty:jetty-util:9.4.15.v20190215",
        "org.eclipse.jetty:jetty-server:9.4.15.v20190215",
        "org.eclipse.jetty:jetty-http:9.4.15.v20190215",
        "javax.servlet:javax.servlet-api:4.0.1",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party:jetty_server9_install.json",
    repositories = MAVEN_REPOS,
)

load("@jetty_server9//:defs.bzl", pin_jetty_server9 = "pinned_maven_install")

pin_jetty_server9()

maven_install(
    name = "lombok",
    artifacts = [
        "org.projectlombok:lombok:1.18.16",
    ],
    fetch_sources = True,
    repositories = MAVEN_REPOS,
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
    fetch_sources = True,
    repositories = MAVEN_REPOS,
)

maven_install(
    name = "selenium",
    artifacts = [
        "com.codeborne:phantomjsdriver:1.4.4",
        "com.google.code.findbugs:jsr305:3.0.2",
        "com.google.errorprone:error_prone_annotations:2.3.3",
        "com.google.guava:guava:25.1-jre",
        "commons-codec:commons-codec:1.11",
        "commons-io:commons-io:2.5",
        "net.bytebuddy:byte-buddy:1.10.1",
        "org.seleniumhq.selenium:selenium-android-driver:2.39.0",
        "org.seleniumhq.selenium:selenium-api:3.8.1",
        "org.seleniumhq.selenium:selenium-chrome-driver:3.8.1",
        "org.seleniumhq.selenium:selenium-firefox-driver:3.8.1",
        "org.seleniumhq.selenium:selenium-htmlunit-driver:2.52.0",
        "org.seleniumhq.selenium:selenium-ie-driver:3.8.1",
        "org.seleniumhq.selenium:selenium-iphone-driver:2.39.0",
        "org.seleniumhq.selenium:selenium-java:3.8.1",
        "org.seleniumhq.selenium:selenium-remote-driver:3.8.1",
        "org.seleniumhq.selenium:selenium-safari-driver:3.8.1",
        "org.seleniumhq.selenium:selenium-support:3.8.1",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party:selenium_install.json",
    repositories = MAVEN_REPOS,
)

load("@selenium//:defs.bzl", pin_selenium = "pinned_maven_install")

pin_selenium()

maven_install(
    name = "com_salesforce_servicelibs_grpc_testing_contrib",
    artifacts = [
        "com.salesforce.servicelibs:grpc-testing-contrib:0.8.1",
    ],
    excluded_artifacts = [
        "io.netty:*",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party:com_salesforce_servicelibs_grpc_testing_contrib_install.json",
    repositories = MAVEN_REPOS,
)

load("@com_salesforce_servicelibs_grpc_testing_contrib//:defs.bzl", com_salesforce_servicelibs_grpc_testing_contrib_pin = "pinned_maven_install")

com_salesforce_servicelibs_grpc_testing_contrib_pin()

http_archive(
    name = "bazel_sonarqube",
    sha256 = "53c8eb6ede402a6cc1e9d38bbf8b7285d13cc86e3b30875f2969582adc918afb",
    strip_prefix = "bazel-sonarqube-56537ff1cf4e6c28fba2b06e0f20d1f4e186645e",
    url = "https://github.com/Zetten/bazel-sonarqube/archive/56537ff1cf4e6c28fba2b06e0f20d1f4e186645e.tar.gz",
)

load("@bazel_sonarqube//:repositories.bzl", "bazel_sonarqube_repositories")

bazel_sonarqube_repositories()
