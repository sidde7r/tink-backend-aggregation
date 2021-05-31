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

versions.check("3.0.0", "3.7.2")

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

TINK_HTTPCORE_COMMIT = "0f72fa2c392fee8388d327cb3462cd10d675c2e2"

http_archive(
    name = "tink_httpcore_4_4_9",
    sha256 = "781ef2bb54bff4189b2dc6dc3e7dadab24908952f0310d7584443cb9336878b5",
    strip_prefix = "httpcomponents-core-{}".format(TINK_HTTPCORE_COMMIT),
    urls = [
        "https://github.com/tink-ab/httpcomponents-core/archive/{}.tar.gz".format(TINK_HTTPCORE_COMMIT),
    ],
)

TINK_HTTPCLIENT_COMMIT = "1ed65fa09a4b7bc9f469fbb3625ac5b087f9cc3e"

http_archive(
    name = "tink_httpclient_4_5_5",
    sha256 = "fa8d45d3e10db9e8a52dc30515931e93be6f7ad33ffd2aa1976a62a86aa75e73",
    strip_prefix = "httpcomponents-client-{}".format(TINK_HTTPCLIENT_COMMIT),
    urls = [
        "https://github.com/tink-ab/httpcomponents-client/archive/{}.tar.gz".format(TINK_HTTPCLIENT_COMMIT),
    ],
)

git_repository(
    name = "com_tink_api_grpc",
    commit = "441d17df88ad3bdbebd2304e8d87bfeb35b1b090",
    remote = "git@github.com:tink-ab/tink-grpc.git",
    shallow_since = "1613035073 +0000",
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
    sha256 = "b85f48fa105c4403326e9525ad2b2cc437babaa6e15a3fc0b1dbab0ab064bc7c",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-gazelle/releases/download/v0.22.2/bazel-gazelle-v0.22.2.tar.gz",
        "https://github.com/bazelbuild/bazel-gazelle/releases/download/v0.22.2/bazel-gazelle-v0.22.2.tar.gz",
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

# Broken dependency explicitly added
# keep
go_repository(
    name = "com_github_google_go_containerregistry",
    importpath = "github.com/google/go-containerregistry",
    sha256 = "44ab9b4a959950922ca11f9fe511010f649c2af034dace12da37a33a4fae3ded",
    strip_prefix = "google-go-containerregistry-e5f4efd",
    type = "tar.gz",
    urls = ["https://api.github.com/repos/google/go-containerregistry/tarball/e5f4efd48dbff3ab3165a944d6777f8db28f0ccb"],  # v0.1.2
)

git_repository(
    name = "tink_backend",
    commit = "9db7daf9a85c050eb782b9cc83cf9b3b9b28a4ef",
    remote = "git@github.com:tink-ab/tink-backend.git",
    shallow_since = "1601479333 +0000",
)

# To be used only by //src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework
git_repository(
    name = "tink_backend_for_agents_framework",
    commit = "288131fcf5d52cbfaaa9a83896429187c2376cb4",
    remote = "git@github.com:tink-ab/tink-backend.git",
    shallow_since = "1595000000 +0000",
)

new_git_repository(
    name = "dropwizard_jersey",
    build_file = "//third_party:dropwizard_jersey.BUILD",
    commit = "0c2f90f4358e262d0fe0af3f6d31eb0fa3cabc40",
    remote = "git@github.com:tink-ab/dropwizard.git",
    shallow_since = "1490898663 +0200",
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
    digest = "sha256:ef522c093c11cb2060e14d9a26ef8651c4b2d8fed96556abf0511d49f1bf1a62",
    registry = "gcr.io",
    repository = "tink-containers/openjdk-8-jre",
    tag = "8",
)

container_pull(
    name = "openjdk_jdk8",
    digest = "sha256:3a69bc28fd0c481e15364089ce2bd717117ec1df507c3a5482b7aea129e06f75",
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
    urls = [
        "https://mirror.deepines.com/deepin/pool/main/libm/libm4ri/libm4ri-dev_20140914-2%2Bb1_amd64.deb",
        "http://ftp.br.debian.org/debian/pool/main/libm/libm4ri/libm4ri-dev_20140914-2+b1_amd64.deb",
    ],
)

http_file(
    name = "libm4ri_0.0.20140914",
    downloaded_file_path = "libm4ri-0.0.20140914_20140914-2+b1_amd64.deb",
    sha256 = "c2f38d51730b6e9a73e2f4d2e0edfadf647a9889da9d06a15abca07d3eccc6f1",
    urls = [
        "https://mirror.deepines.com/deepin/pool/main/libm/libm4ri/libm4ri-0.0.20140914_20140914-2%2Bb1_amd64.deb",
        "http://ftp.br.debian.org/debian/pool/main/libm/libm4ri/libm4ri-0.0.20140914_20140914-2+b1_amd64.deb",
    ],
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
PROTOBUF_VERSION = "3.11.3"

# Keep in mind the netty version compatibility table linked below when updating this
# https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty
GRPC_JAVA_VERSION = "1.29.0"

IO_NETTY_VERSION = "4.1.50.Final"

IO_NETTY_BORINGSSL_VERSION = "2.0.30.Final"

GRPC_JAVA_NANO_VERSION = "1.21.1"

ECLIPSE_JETTY_VERSION = "9.1.0.M0"

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
    sha256 = "832c476bb442ca98a59c2291b8a504648d1c139b74acc15ef667a0e8f5e984e7",
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

OPENCENSUS_VERSION = "0.19.2"

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

jvm_maven_import_external(
    name = "joda_time_joda_time",
    artifact = "joda-time:joda-time:2.9.9",
    artifact_sha256 = "b049a43c1057942e6acfbece008e4949b2e35d1658d0c8e06f4485397e2fa4e7",
    licenses = ["notice"],
    server_urls = ["https://repo1.maven.org/maven2"],
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

RULES_JVM_EXTERNAL_TAG = "4.0"

RULES_JVM_EXTERNAL_SHA = "31701ad93dbfe544d597dbe62c9a1fdd76d81d8a9150c2bf1ecf928ecdf97169"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

maven_install(
    name = "maven",
    artifacts = [
        "aopalliance:aopalliance:1.0",
        "ch.qos.logback:logback-classic:1.1.11",
        "ch.qos.logback:logback-core:1.1.11",
        "com.codahale.metrics:metrics-annotation:3.0.2",
        "com.codahale.metrics:metrics-core:3.0.2",
        "com.codahale.metrics:metrics-healthchecks:3.0.2",
        "com.codahale.metrics:metrics-jersey:3.0.2",
        "com.codahale.metrics:metrics-jetty9:3.0.2",
        "com.codahale.metrics:metrics-json:3.0.2",
        "com.codahale.metrics:metrics-jvm:3.0.2",
        "com.codahale.metrics:metrics-logback:3.0.2",
        "com.codahale.metrics:metrics-servlets:3.0.2",
        "com.fasterxml.jackson.core:jackson-annotations:2.9.10",
        "com.fasterxml.jackson.core:jackson-core:2.9.10",
        "com.fasterxml.jackson.core:jackson-databind:2.9.10.8",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.9",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-base:2.9.9",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.9.9",
        "com.fasterxml.jackson.module:jackson-module-afterburner:2.9.9",
        "com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.9.9",
        "com.fasterxml:classmate:1.0.0",
        "com.github.rholder:guava-retrying:2.0.0",
        "com.google.inject:guice:4.2.2",
        "com.googlecode.gettext-commons:gettext-commons:0.9.8",
        "com.googlecode.libphonenumber:libphonenumber:5.7",
        "com.sun.jersey:jersey-client:1.18.1",
        "com.sun.jersey:jersey-core:1.18.1",
        "com.sun.jersey:jersey-server:1.18.1",
        "com.sun.jersey:jersey-servlet:1.18.1",
        "com.sun.xml.bind:jaxb-impl:2.2.3-1",
        "com.uber.nullaway:nullaway:0.7.6",
        "commons-beanutils:commons-beanutils:1.7.0",
        "commons-cli:commons-cli:1.4",
        "commons-codec:commons-codec:1.6",
        "commons-collections:commons-collections:3.2.2",
        "commons-lang:commons-lang:2.6",
        "commons-logging:commons-logging:1.1.3",
        "commons-validator:commons-validator:1.7",
        "io.dropwizard:dropwizard-configuration:0.7.1",
        "io.dropwizard:dropwizard-core:0.7.1",
        "io.dropwizard:dropwizard-jackson:0.7.1",
        "io.dropwizard:dropwizard-jetty:0.7.1",
        "io.dropwizard:dropwizard-lifecycle:0.7.1",
        "io.dropwizard:dropwizard-logging:0.7.1",
        "io.dropwizard:dropwizard-metrics:0.7.1",
        "io.dropwizard:dropwizard-servlets:0.7.1",
        "io.dropwizard:dropwizard-util:0.7.1",
        "io.dropwizard:dropwizard-validation:0.7.1",
        "io.jaegertracing:jaeger-core:1.0.0",
        "io.jaegertracing:jaeger-thrift:1.0.0",
        "io.netty:netty-buffer:4.1.50.Final",
        "io.netty:netty-codec-http2:4.1.50.Final",
        "io.netty:netty-codec:4.1.50.Final",
        "io.netty:netty-common:4.1.50.Final",
        "io.netty:netty-handler:4.1.50.Final",
        "io.netty:netty-resolver:4.1.50.Final",
        "io.netty:netty-transport:4.1.50.Final",
        "io.opencensus:opencensus-api:0.21.0",
        "io.opencensus:opencensus-contrib-grpc-metrics:0.21.0",
        "io.opentracing.contrib:opentracing-grpc:0.2.0",
        "io.opentracing:opentracing-api:0.33.0",
        "io.opentracing:opentracing-noop:0.33.0",
        "io.opentracing:opentracing-util:0.33.0",
        "io.perfmark:perfmark-api:0.17.0",
        "io.prometheus:simpleclient:0.6.0",
        "io.prometheus:simpleclient_common:0.6.0",
        "io.prometheus:simpleclient_hotspot:0.6.0",
        "io.prometheus:simpleclient_httpserver:0.6.0",
        "io.prometheus:simpleclient_pushgateway:0.6.0",
        "io.prometheus:simpleclient_servlet:0.6.0",
        "io.protostuff:protostuff-api:1.5.1",
        "io.swagger:swagger-annotations:1.5.9",
        "jakarta.xml.bind:jakarta.xml.bind-api:2.3.3",
        "javax.activation:activation:1.1",
        "javax.annotation:javax.annotation-api:1.3.2",
        "javax.annotation:jsr250-api:1.0",
        "javax.el:javax.el-api:2.2.5",
        "javax.inject:javax.inject:1",
        "javax.validation:validation-api:2.0.1.Final",
        "javax.xml.bind:jaxb-api:2.3.1",
        "net.sourceforge.argparse4j:argparse4j:0.4.3",
        "no.finn.unleash:unleash-client-java:4.1.0",
        "org.apache.commons:commons-lang3:3.4",
        "org.apache.curator:curator-client:4.3.0",
        "org.apache.curator:curator-framework:4.3.0",
        "org.apache.curator:curator-recipes:4.3.0",
        "org.apache.httpcomponents:httpclient:4.5.12",
        "org.apache.httpcomponents:httpcore:4.4.13",
        "org.apache.kafka:kafka-clients:2.3.1",
        "org.apache.kafka:kafka-streams:2.3.1",
        "org.apache.logging.log4j:log4j-api:2.11.1",
        "org.apache.logging.log4j:log4j-core:2.11.1",
        "org.apache.thrift:libthrift:0.12.0",
        "org.apache.zookeeper:zookeeper:3.5.3-beta",
        "org.bouncycastle:bcpkix-jdk15on:1.68",
        "org.bouncycastle:bcprov-jdk15on:1.68",
        "org.eclipse.jetty.orbit:javax.servlet:3.0.0.v201112011016",
        "org.eclipse.jetty.toolchain.setuid:jetty-setuid-java:1.0.2",
        "org.eclipse.jetty:jetty-continuation:9.0.7.v20131107",
        "org.eclipse.jetty:jetty-http:9.0.7.v20131107",
        "org.eclipse.jetty:jetty-io:9.0.7.v20131107",
        "org.eclipse.jetty:jetty-proxy:9.0.7.v20131107",
        "org.eclipse.jetty:jetty-security:9.0.7.v20131107",
        "org.eclipse.jetty:jetty-server:9.0.7.v20131107",
        "org.eclipse.jetty:jetty-servlet:9.0.7.v20131107",
        "org.eclipse.jetty:jetty-servlets:9.0.7.v20131107",
        "org.eclipse.jetty:jetty-util:9.0.7.v20131107",
        "org.glassfish.jaxb:jaxb-runtime:2.3.3",
        "org.glassfish.web:javax.el:2.2.6",
        "org.iban4j:iban4j:3.1.0",
        "org.reflections:reflections:0.9.11",
        maven.artifact(
            group = "de.jollyday",
            artifact = "jollyday",
            version = "0.5.10",
            exclusions = ["org.threeten:threeten-extra"],
        ),
    ],
    excluded_artifacts = [
        # These are dependencies by org.apache.zookeeper:zookeeper, and are colliding
        # with log4j-over-slf4j. Excluding it here instead of only from zookeeper
        # as there's little to no reason to ever include it.
        "org.slf4j:slf4j-log4j12",
        "log4j:log4j",

        # Exclude ALL artifacts that are currently managed with maven_jar
        # This is neccesary to make sure that we're not transiently depending
        # on a different version of these dependencies.

        # TODO: Migrate all dependencies below to be managed by maven_install
        "com.google.api.grpc:proto-google-common-protos",
        "com.google.code.findbugs:jsr305",
        "com.google.code.gson:gson",
        "com.google.crypto.tink:tink",
        "com.google.errorprone:error_prone_annotations",
        "com.google.errorprone:error_prone_annotations",
        "com.google.errorprone:javac-shaded",
        "com.google.googlejavaformat:google-java-format",
        "com.google.guava:guava",
        "com.google.http-client:google-http-client",
        "com.google.instrumentation:instrumentation-api",
        "com.google.protobuf:protobuf-java-util",
        "com.google.protobuf:protobuf-java",
        "com.googlecode.concurrent-trees:concurrent-trees",
        "com.googlecode.concurrentlinkedhashmap:concurrentlinkedhashmap-lru",
        "de.grundid.opendatalab:geojson-jackson",
        "eu.geekplace.javapinning:java-pinning-jar",
        "hsqldb:hsqldb",
        "io.grpc:grpc-api",
        "io.grpc:grpc-auth",
        "io.grpc:grpc-context",
        "io.grpc:grpc-core",
        "io.grpc:grpc-netty",
        "io.grpc:grpc-protobuf-lite",
        "io.grpc:grpc-protobuf-nano",
        "io.grpc:grpc-protobuf",
        "io.grpc:grpc-services",
        "io.grpc:grpc-stub",
        "io.grpc:grpc-testing-proto",
        "io.grpc:grpc-testing",
        "jfree:jcommon",
        "jline:jline",
        "joda-time:joda-time",
        "junit:junit",
        "mx4j:mx4j-tools",
        "mysql:mysql-connector-java",
        "net.bytebuddy:byte-buddy",
        "net.jadler:jadler-all",
        "net.java.dev.jets3t:jets3t",
        "net.java.dev.jna:jna",
        "net.java.dev.jna:platform",
        "net.jcip:jcip-annotations",
        "net.jodah:failsafe",
        "net.jpountz.lz4:lz4",
        "org.elasticsearch:elasticsearch",
        "org.hamcrest:hamcrest-all",
        "org.hibernate:hibernate-annotations",
        "org.hibernate:hibernate-commons-annotations",
        "org.hibernate:hibernate-core",
        "org.hibernate:hibernate-entitymanager",
        "org.hibernate:hibernate-validator",
        "org.hibernate.javax.persistence:hibernate-jpa-2.0-api",
        "org.javassist:javassist",
        "org.jboss.logging:jboss-logging",
        "org.jfree:jfreesvg",
        "org.joda:joda-convert",
        "org.json:json",
        "org.jsoup:jsoup",
        "org.jvnet.jaxb2_commons:jaxb2-basics-runtime",
        "org.slf4j:jcl-over-slf4j",
        "org.slf4j:jul-to-slf4j",
        "org.slf4j:slf4j-api",
        "org.w3c.css:sac",
        "org.webbitserver:webbit",
        "org.xerial.snappy:snappy-java",
        "org.xmlunit:xmlunit-core",
        "org.xmlunit:xmlunit-legacy",
        "org.yaml:snakeyaml",
        "oro:oro",
        "pl.pragmatists:JUnitParams",
        "stax:stax-api",
        "tomcat:jasper-compiler",
        "tomcat:jasper-runtime",
        "xalan:serializer",
        "xalan:xalan",
        "xerces:xercesImpl",
        "xml-apis:xml-apis-ext",
        "xml-apis:xml-apis",
        "xmlenc:xmlenc",
        "xmlpull:xmlpull",
        "xpp3:xpp3_min",
    ],
    override_targets = {
        "aopalliance:aopalliance": "@aggregation//:aopalliance_aopalliance",
        "ch.qos.logback:logback-classic": "@aggregation//:ch_qos_logback_logback_classic",
        "ch.qos.logback:logback-core": "@aggregation//:ch_qos_logback_logback_core",
        "com.codahale.metrics:metrics-annotation": "@aggregation//:com_codahale_metrics_metrics_annotation",
        "com.codahale.metrics:metrics-core": "@aggregation//:com_codahale_metrics_metrics_core",
        "com.codahale.metrics:metrics-healthchecks": "@aggregation//:com_codahale_metrics_metrics_healthchecks",
        "com.codahale.metrics:metrics-jersey": "@aggregation//:com_codahale_metrics_metrics_jersey",
        "com.codahale.metrics:metrics-jetty9": "@aggregation//:com_codahale_metrics_metrics_jetty9",
        "com.codahale.metrics:metrics-json": "@aggregation//:com_codahale_metrics_metrics_json",
        "com.codahale.metrics:metrics-jvm": "@aggregation//:com_codahale_metrics_metrics_jvm",
        "com.codahale.metrics:metrics-logback": "@aggregation//:com_codahale_metrics_metrics_logback",
        "com.codahale.metrics:metrics-servlets": "@aggregation//:com_codahale_metrics_metrics_servlets",
        "com.fasterxml.jackson.core:jackson-annotations": "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "com.fasterxml.jackson.core:jackson-core": "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "com.fasterxml.jackson.core:jackson-databind": "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-smile": "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_smile",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml": "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_yaml",
        "com.fasterxml.jackson.datatype:jackson-datatype-guava": "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_guava",
        "com.fasterxml.jackson.datatype:jackson-datatype-jdk8": "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_jdk8",
        "com.fasterxml.jackson.datatype:jackson-datatype-joda": "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_joda",
        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310": "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-base": "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_base",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider": "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
        "com.fasterxml.jackson.module:jackson-module-afterburner": "@aggregation//:com_fasterxml_jackson_module_jackson_module_afterburner",
        "com.fasterxml.jackson.module:jackson-module-jaxb-annotations": "@aggregation//:com_fasterxml_jackson_module_jackson_module_jaxb_annotations",
        "com.fasterxml:classmate": "@aggregation//:com_fasterxml_classmate",
        "com.github.luben:zstd-jni": "@aggregation//:com_github_luben_zstd_jni",
        "com.github.rholder:guava-retrying": "@aggregation//:com_github_rholder_guava_retrying",
        "com.google.inject:guice": "@aggregation//:com_google_inject_guice",
        "com.googlecode.gettext-commons:gettext-commons": "@aggregation//:com_googlecode_gettext_commons_gettext_commons",
        "com.googlecode.libphonenumber:libphonenumber": "@aggregation//:com_googlecode_libphonenumber_libphonenumber",
        "com.sangupta:murmur": "@aggregation//:com_sangupta_murmur",
        "com.squareup.okhttp3:okhttp": "@aggregation//:com_squareup_okhttp3_okhttp",
        "com.squareup.okio:okio": "@aggregation//:com_squareup_okio_okio",
        "com.sun.activation:jakarta.activation": "@aggregation//:com_sun_activation_jakarta_activation",
        "com.sun.istack:istack-commons-runtime": "@aggregation//:com_sun_istack_istack_commons_runtime",
        "com.sun.jersey:jersey-client": "@aggregation//:com_sun_jersey_jersey_client",
        "com.sun.jersey:jersey-core": "@aggregation//:com_sun_jersey_jersey_core",
        "com.sun.jersey:jersey-server": "@aggregation//:com_sun_jersey_jersey_server",
        "com.sun.jersey:jersey-servlet": "@aggregation//:com_sun_jersey_jersey_servlet",
        "com.sun.xml.bind:jaxb-impl": "@aggregation//:com_sun_xml_bind_jaxb_impl",
        "com.uber.nullaway:nullaway": "@aggregation//:com_uber_nullaway_nullaway",
        "commons-beanutils:commons-beanutils": "@aggregation//:commons_beanutils_commons_beanutils",
        "commons-cli:commons-cli": "@aggregation//:commons_cli_commons_cli",
        "commons-codec:commons-codec": "@aggregation//:commons_codec_commons_codec",
        "commons-collections:commons-collections": "@aggregation//:commons_collections_commons_collections",
        "commons-digester:commons-digester": "@aggregation//:commons_digester_commons_digester",
        "commons-io:commons-io:2.6": "@aggregation//:commons_io_commons_io",
        "commons-lang:commons-lang": "@aggregation//:commons_lang_commons_lang",
        "commons-logging:commons-logging": "@aggregation//:commons_logging_commons_logging",
        "io.opencensus:opencensus-api": "@aggregation//:io_opencensus_opencensus_api",
        "io.opencensus:opencensus-contrib-grpc-metrics": "@aggregation//:io_opencensus_opencensus_contrib_grpc_metrics",
        "org.apache.curator:curator-client": "@aggregation//:org_apache_curator_curator_client",
        "org.apache.curator:curator-framework": "@aggregation//:org_apache_curator_curator_framework",
        "org.apache.curator:curator-recipes": "@aggregation//:org_apache_curator_curator_recipes",
        "org.apache.curator:curator-x-discovery": "@aggregation//:org_apache_curator_curator_x_discovery",
        "org.apache.zookeeper:zookeeper": "@aggregation//:org_apache_zookeeper_zookeeper",
        "org.eclipse.jetty:jetty-client": "@aggregation//:org_eclipse_jetty_jetty_client",
        "org.eclipse.jetty:jetty-continuation": "@aggregation//:org_eclipse_jetty_jetty_continuation",
        "org.eclipse.jetty:jetty-http": "@aggregation//:org_eclipse_jetty_jetty_http",
        "org.eclipse.jetty:jetty-io": "@aggregation//:org_eclipse_jetty_jetty_io",
        "org.eclipse.jetty:jetty-security": "@aggregation//:org_eclipse_jetty_jetty_security",
        "org.eclipse.jetty:jetty-server": "@aggregation//:org_eclipse_jetty_jetty_server",
        "org.eclipse.jetty:jetty-servlet": "@aggregation//:org_eclipse_jetty_jetty_servlet",
        "org.eclipse.jetty:jetty-servlets": "@aggregation//:org_eclipse_jetty_jetty_servlets",
        "org.eclipse.jetty:jetty-util": "@aggregation//:org_eclipse_jetty_jetty_util",
        "org.slf4j:log4j-over-slf4j": "@aggregation//:org_slf4j_log4j_over_slf4j",
    },
    fetch_sources = True,
    maven_install_json = "//third_party/maven:maven_install.json",
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
    version_conflict_policy = "pinned",
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

load("@cryptography_lib//:defs.bzl", cryptography_lib_pin = "pinned_maven_install")

cryptography_lib_pin()

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
        "ch.qos.logback:logback-classic:1.2.3",
        "ch.qos.logback:logback-core:1.2.3",
        "com.amazonaws:aws-java-sdk-code-generator:1.11.381",
        "com.amazonaws:aws-java-sdk-kms:1.11.381",
        "com.amazonaws:aws-java-sdk-s3:1.11.381",
        "com.amazonaws:aws-java-sdk-sqs:1.11.381",
        "com.amazonaws:jmespath-java:1.11.381",
        "com.auth0:java-jwt:3.3.0",
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
        "com.fasterxml.jackson.core:jackson-annotations:2.10.5",
        "com.fasterxml.jackson.core:jackson-core:2.10.5",
        "com.fasterxml.jackson.core:jackson-databind:2.10.5.1",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.9.9",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.10.0",
        "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.9.9",
        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.9",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-base:2.9.9",
        "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.9.9",
        "com.fasterxml.jackson.module:jackson-module-afterburner:2.9.9",
        "com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.9.9",
        "com.fasterxml.jackson.module:jackson-module-jsonSchema:2.9.9",
        "com.fasterxml.uuid:java-uuid-generator:3.1.5",
        "com.fasterxml:classmate:1.0.0",
        "com.flipkart.zjsonpatch:zjsonpatch:0.2.1",
        "com.github.docker-java:docker-java-api:3.2.5",
        "com.github.jai-imageio:jai-imageio-core:1.4.0",
        "com.github.javafaker:javafaker:1.0.2",
        "com.github.luben:zstd-jni:1.4.0-1",
        "com.github.rholder:guava-retrying:2.0.0",
        "com.github.stephenc.jcip:jcip-annotations:1.0-1",
        "com.github.tomakehurst:wiremock-standalone:2.27.1",
        "com.google.api.grpc:proto-google-common-protos:0.1.9",
        "com.google.code.findbugs:jsr305:3.0.2",
        "com.google.code.gson:gson:2.8.6",
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
        "com.googlecode.libphonenumber:libphonenumber:5.7",
        "com.jayway.jsonpath:json-path:2.4.0",
        "com.jcraft:jzlib:1.1.3",
        "com.kjetland:mbknor-jackson-jsonschema_2.12:1.0.34",
        "com.lambdaworks:scrypt:1.3.2",
        "com.netflix.governator:governator-api:1.17.2",
        "com.netflix.governator:governator-core:1.17.2",
        "com.netflix.governator:governator:1.17.2",
        "com.nimbusds:nimbus-jose-jwt:8.20.1",
        "com.nimbusds:srp6a:2.0.2",
        "com.opsgenie.integration:sdk-shaded:jar:2.8.2",
        "com.oracle.substratevm:svm:19.0.0",
        "com.squareup.okhttp3:okhttp:4.9.0",
        "com.squareup.okio:okio:2.8.0",
        "com.sun.activation:jakarta.activation:1.2.2",
        "com.sun.istack:istack-commons-runtime:3.0.11",
        "com.sun.jersey.contribs:jersey-apache-client4:1.18.1",
        "com.sun.jersey:jersey-client:1.18.1",
        "com.sun.jersey:jersey-core:1.18.1",
        "com.sun.jersey:jersey-server:1.18.1",
        "com.sun.jersey:jersey-servlet:1.18.1",
        "com.sun.xml.bind:jaxb-impl:2.2.3-1",
        "com.uber.nullaway:nullaway:0.7.6",
        "com.yubico:yubico-validation-client2:2.0.1",
        "commons-cli:commons-cli:1.4",
        "commons-codec:commons-codec:1.11",
        "commons-collections:commons-collections:3.2.2",
        "commons-httpclient:commons-httpclient:3.1",
        "commons-io:commons-io:2.6",
        "commons-lang:commons-lang:2.6",
        "commons-logging:commons-logging:1.2",
        "commons-validator:commons-validator:1.7",
        "de.jollyday:jollyday:0.4.7",
        "dom4j:dom4j:1.6.1",
        "eu.geekplace.javapinning:java-pinning-jar:1.0.1",
        "io.dropwizard:dropwizard-client:0.7.1",
        "io.dropwizard:dropwizard-configuration:0.7.1",
        "io.dropwizard:dropwizard-core:0.7.1",
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
        "io.netty:netty:3.10.6.Final",
        "io.opencensus:opencensus-api:%s" % OPENCENSUS_VERSION,
        "io.opencensus:opencensus-contrib-grpc-metrics:%s" % OPENCENSUS_VERSION,
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
        "javax.xml.bind:jaxb-api:2.3.1",
        "javax.xml.stream:stax-api:1.0-2",
        "jline:jline:0.9.94",
        "joda-time:joda-time:2.9.9",
        "junit:junit:4.12",
        "mysql:mysql-connector-java:5.1.42",
        "net.bytebuddy:byte-buddy-agent:1.10.1",
        "net.bytebuddy:byte-buddy:1.10.1",
        "net.jadler:jadler-all:1.3.0",
        "net.java.dev.jna:jna:5.5.0",
        "net.minidev:asm:1.0.2",
        "net.minidev:json-smart:2.3",
        "net.sf.jopt-simple:jopt-simple:4.9",
        "net.sourceforge.argparse4j:argparse4j:0.4.3",
        "net.sourceforge.cssparser:cssparser:0.9.16",
        "net.sourceforge.lept4j:lept4j:1.10.0",
        "net.sourceforge.tess4j:tess4j:4.0.2",
        "net.spy:spymemcached:2.9.1",
        "no.finn.unleash:unleash-client-java:4.1.0",
        "org.apache.commons:commons-collections4:4.0",
        "org.apache.commons:commons-csv:1.0",
        "org.apache.commons:commons-lang3:3.9",
        "org.apache.commons:commons-math3:3.2",
        "org.apache.commons:commons-text:1.8",
        "org.apache.curator:curator-client:4.1.0",
        "org.apache.curator:curator-framework:4.1.0",
        "org.apache.curator:curator-recipes:4.1.0",
        "org.apache.curator:curator-x-discovery:4.0.0",
        "org.apache.httpcomponents:httpclient:4.5.12",
        "org.apache.httpcomponents:httpcore:4.4.13",
        "org.apache.logging.log4j:log4j-api:2.11.1",
        "org.apache.logging.log4j:log4j-core:2.11.1",
        "org.apache.mahout.commons:commons-cli:2.0-mahout",
        "org.apache.pdfbox:fontbox:2.0.9",
        "org.apache.pdfbox:pdfbox:2.0.9",
        "org.apache.zookeeper:zookeeper:3.5.4-beta",
        "org.aspectj:aspectjrt:1.8.10",
        "org.assertj:assertj-core:3.8.0",
        "org.bitbucket.b_c:jose4j:0.6.5",
        "org.bouncycastle:bcpkix-jdk15on:1.68",
        "org.bouncycastle:bcprov-jdk15on:1.68",
        "org.codehaus.jackson:jackson-mapper-asl:1.9.13",
        "org.codehaus.plexus:plexus-utils:3.0.17",
        "org.codehaus.woodstox:stax2-api:4.1",
        "org.eclipse.jetty.orbit:javax.servlet:3.0.0.v201112011016",
        "org.eclipse.jetty.toolchain.setuid:jetty-setuid-java:1.0.2",
        "org.eclipse.jetty:jetty-client:%s" % ECLIPSE_JETTY_VERSION,
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
        "org.hibernate:hibernate-entitymanager:3.5.4-Final",
        "org.hibernate:hibernate-validator:5.1.1.Final",
        "org.iban4j:iban4j:3.1.0",
        "org.javassist:javassist:3.26.0-GA",
        "org.jboss.logging:jboss-logging:3.1.4.GA",
        "org.json:json:20080701",
        "org.jsoup:jsoup:1.13.1",
        "org.mockito:mockito-core:3.0.0",
        "org.modelmapper:modelmapper:1.1.0",
        "org.mozilla:rhino:1.7R4",
        "org.objenesis:objenesis:2.6",
        "org.ow2.asm:asm:5.0.4",
        "org.pojava:pojava:2.8.1",
        "org.projectlombok:lombok:1.18.16",
        "org.reactivestreams:reactive-streams:1.0.3",
        "org.reflections:reflections:0.9.11",
        "org.slf4j:jcl-over-slf4j:1.7.25",
        "org.slf4j:jul-to-slf4j:1.7.25",
        "org.slf4j:slf4j-api:1.7.30",
        "org.slf4j:slf4j-simple:1.7.5",
        "org.springframework.boot:spring-boot-test:2.1.3.RELEASE",
        "org.springframework.data:spring-data-jpa:1.11.1.RELEASE",
        "org.springframework.security:spring-security-core:4.2.3.RELEASE",
        "org.springframework:spring-aop:5.1.5.RELEASE",
        "org.springframework:spring-beans:5.1.5.RELEASE",
        "org.springframework:spring-context:5.1.5.RELEASE",
        "org.springframework:spring-core:5.1.5.RELEASE",
        "org.springframework:spring-expression:5.1.5.RELEASE",
        "org.springframework:spring-jdbc:4.3.9.RELEASE",
        "org.springframework:spring-orm:4.3.9.RELEASE",
        "org.springframework:spring-test:5.1.5.RELEASE",
        "org.springframework:spring-tx:4.3.9.RELEASE",
        "org.springframework:spring-web:5.1.5.RELEASE",
        "org.springframework:spring-webmvc:5.1.5.RELEASE",
        "org.testcontainers:testcontainers:1.15.0-rc2",
        "org.w3c.css:sac:1.3",
        "org.xerial.snappy:snappy-java:1.0.5-M2",
        "org.xmlunit:xmlunit-core:2.1.1",
        "org.xmlunit:xmlunit-legacy:2.1.1",
        "org.yaml:snakeyaml:1.23",
        "pl.pragmatists:JUnitParams:1.0.5",
        "software.amazon.ion:ion-java:1.0.2",
        "xerces:xercesImpl:2.12.0",
        "xml-apis:xml-apis:1.4.01",
    ],
    excluded_artifacts = [
        "org.slf4j:slf4j-log4j12",  # log4j-over-slf4j and slf4j-log4j12 cannot coexist on the classpath
        "javassist:javassist",      # Already covered by the newer org.javassist:javassist
        "com.lowagie:itext",        # Cannot add this one for some reason, but it doesn't seem to be needed anyway
        "log4j:log4j",              # Superseded by Log4J2 (org.apache.logging.log4j:log4j-core)
    ],
    fetch_sources = True,
    generate_compat_repositories = False,  # Tempting, but provided that we depend on tink-backend, let's be explicit in our naming of deps
    maven_install_json = "//third_party:aggregation_install.json",
    repositories = MAVEN_REPOS,
    version_conflict_policy = "default",  # Let's stick to Coursier's algorithm and strive for NO CONFLICTS as far as possible
)

load("@aggregation//:defs.bzl", aggregation_pin = "pinned_maven_install")

aggregation_pin()

# To be moved into the aggregation maven_install eventually, or shaded
maven_install(
    name = "grpc_libraries",
    artifacts = [
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
    ],
    # Exclude ALL transitive dependencies of the artifacts above for now
    excluded_artifacts = [
        "com.google.android:annotations",
        "com.google.api.grpc:proto-google-common-protos",
        "com.google.auth:google-auth-library-credentials",
        "com.google.code.findbugs:jsr305",
        "com.google.code.gson:gson",
        "com.google.errorprone:error_prone_annotations",
        "com.google.guava:failureaccess",
        "com.google.guava:guava",
        "com.google.guava:listenablefuture",
        "com.google.j2objc:j2objc-annotations",
        "com.google.protobuf.nano:protobuf-javanano",
        "com.google.protobuf:protobuf-java",
        "com.google.protobuf:protobuf-java-util",
        "com.google.protobuf:protobuf-javalite",
        "io.grpc:grpc-context",
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
        "junit:junit",
        "org.checkerframework:checker-compat-qual",
        "org.codehaus.mojo:animal-sniffer-annotations",
        "org.hamcrest:hamcrest-core",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party:grpc_libraries_install.json",
    repositories = MAVEN_REPOS,
    version_conflict_policy = "pinned",
)

load("@grpc_libraries//:defs.bzl", grpc_libraries_pin = "pinned_maven_install")

grpc_libraries_pin()

maven_install(
    name = "java_formatter",
    artifacts = [
        "com.google.errorprone:javac-shaded:9+181-r4173-1",
        "com.google.googlejavaformat:google-java-format:1.7",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party:java_formatter_install.json",
    repositories = MAVEN_REPOS,
)

load("@java_formatter//:defs.bzl", java_formatter_pin = "pinned_maven_install")

java_formatter_pin()

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
    name = "selenium",
    artifacts = [
        "com.codeborne:phantomjsdriver:1.4.4",
        "com.google.code.findbugs:jsr305:3.0.2",
        "com.google.code.gson:gson:2.8.6",
        "com.google.errorprone:error_prone_annotations:2.3.3",
        "com.google.guava:guava:25.1-jre",
        "commons-codec:commons-codec:1.11",
        "commons-io:commons-io:2.6",
        "net.bytebuddy:byte-buddy:1.10.1",
        "net.sourceforge.htmlunit:htmlunit:2.37.0",
        "org.apache.httpcomponents:httpclient:4.5.12",
        "org.apache.httpcomponents:httpcore:4.4.13",
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
    name = "browserup_proxy_core",
    artifacts = [
        "com.browserup:browserup-proxy-core:2.1.2",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party/browserup_proxy_core:browserup_proxy_core_install.json",
    repositories = MAVEN_REPOS,
)

load("@browserup_proxy_core//:defs.bzl", pin_browserup_proxy_core = "pinned_maven_install")

pin_browserup_proxy_core()

http_archive(
    name = "bazel_sonarqube",
    sha256 = "53c8eb6ede402a6cc1e9d38bbf8b7285d13cc86e3b30875f2969582adc918afb",
    strip_prefix = "bazel-sonarqube-56537ff1cf4e6c28fba2b06e0f20d1f4e186645e",
    url = "https://github.com/Zetten/bazel-sonarqube/archive/56537ff1cf4e6c28fba2b06e0f20d1f4e186645e.tar.gz",
)

load("@bazel_sonarqube//:repositories.bzl", "bazel_sonarqube_repositories")

bazel_sonarqube_repositories()

# For Nix users

RULES_NIXPKGS_COMMIT = "6178f2aae7a90370f2132abafa977701afc6fb4e"

http_archive(
    name = "io_tweag_rules_nixpkgs",
    sha256 = "e721c383b3d5ca51ad123001d3fb26602aa330ddd9cf2a55d25ddd956c98030a",
    strip_prefix = "rules_nixpkgs-{}".format(RULES_NIXPKGS_COMMIT),
    urls = ["https://github.com/tweag/rules_nixpkgs/archive/{}.tar.gz".format(RULES_NIXPKGS_COMMIT)],
)

load("@io_tweag_rules_nixpkgs//nixpkgs:nixpkgs.bzl", "nixpkgs_git_repository", "nixpkgs_python_configure")

nixpkgs_git_repository(
    name = "nixpkgs_repository",
    revision = "037936b7a307c7399cf0f3d9fabe37ea5b0b8534",
    sha256 = "a6178b795602924f94b081b459fcb868cebc7d3d7d0c6e90306aba850beec629",
)

nixpkgs_python_configure(
    repository = "@nixpkgs_repository",
)

# downloads and extracts binaries used for selenium WebDriver creation

load("//tools/bzl:download_extract_archive.bzl", "download_extract_archive")

download_extract_archive(
    name = "chromedriver",
    linux_path_to_binary = "/chromedriver_linux64",
    linux_sha256 =
        "1a0cd0913d2e92899576491b36871f7218a13c90c85eee9cead6295070016b03",
    linux_url =
        "https://commondatastorage.googleapis.com/chromium-browser-snapshots/Linux_x64/843981/chromedriver_linux64.zip",
    macos_path_to_binary = "/chromedriver_mac64",
    macos_sha256 =
        "bb4b63dc8ad5056504846088751cf20fe1b64832bb8f8ac0328de4c2c0649955",
    macos_url =
        "https://commondatastorage.googleapis.com/chromium-browser-snapshots/Mac/843982/chromedriver_mac64.zip",
)

download_extract_archive(
    name = "chromium",
    linux_path_to_binary = "/",
    linux_sha256 =
        "073ab34347bf84fe8edc909ce7a7c698c0efc9bed472945b7ae6c7ae4b0e36f7",
    linux_url =
        "https://commondatastorage.googleapis.com/chromium-browser-snapshots/Linux_x64/843981/chrome-linux.zip",
    macos_path_to_binary = "/",
    macos_sha256 =
        "51fe6bc1188dfe51c78c008d05bb59fe4f0ae6d7ce88b0f1e08ae23be7c92fcd",
    macos_url =
        "https://commondatastorage.googleapis.com/chromium-browser-snapshots/Mac/843982/chrome-mac.zip",
)
