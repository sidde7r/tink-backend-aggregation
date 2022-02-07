# Use the new Starlark version of git_repository. This uses the system's native
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
    commit = "708406e96a648ac7ab4d15534abb837d51192505",
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
    commit = "c6b0ce582af364b77819a23646174a054f95f7ca",
    remote = "git@github.com:tink-ab/tink-backend.git",
    shallow_since = "1643800000 +0000",
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
    name = "distroless_java_11",
    digest = "sha256:36c4fe3c58205f65a5e6ee3e960452fc49dd96196b845b0a9014bdd95d571d32",
    registry = "gcr.io",
    repository = "tink-containers/distroless_java",
    tag = "11",
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
    downloaded_file_path = "libwebp6_0.6.1-2+deb10u1_amd64.deb",
    sha256 = "f4d8e88f87f41530bbe8ad45f60ab87e313ef1ebc9035f9ff24649fc9dd746a9",
    urls = ["http://ftp.se.debian.org/debian/pool/main/libw/libwebp/libwebp6_0.6.1-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libopenjp2-7_2.3.0-2",
    downloaded_file_path = "libopenjp2-7_2.3.0-2+deb10u2_amd64.deb",
    sha256 = "55127318744936099e6979136c66bf6be6528151639215f657891cfe021cfd56",
    urls = ["http://ftp.se.debian.org/debian/pool/main/o/openjpeg2/libopenjp2-7_2.3.0-2+deb10u2_amd64.deb"],
)

http_file(
    name = "libc6_2.28-10",
    downloaded_file_path = "libc6_2.28-10_amd64.deb",
    sha256 = "6f703e27185f594f8633159d00180ea1df12d84f152261b6e88af75667195a79",
    urls = ["http://ftp.se.debian.org/debian/pool/main/g/glibc/libc6_2.28-10_amd64.deb"],
)

http_file(
    name = "libgif7_5.1.4-3",
    downloaded_file_path = "libgif7_5.1.4-3_amd64.deb",
    sha256 = "a7d7610a798cf3d72bf5ef9f6e44c4b0669f5df3e4a0014e83f9d788ce47f9a9",
    urls = ["http://ftp.us.debian.org/debian/pool/main/g/giflib/libgif7_5.1.4-3_amd64.deb"],
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
    name = "io_grpc_grpc_auth",
    artifact = "io.grpc:grpc-auth:%s" % GRPC_JAVA_VERSION,
    artifact_sha256 = "c0bd4ad63b905823061919f8bdd7371f45dc680ffa208d212fdc228515b01770",
    licenses = ["notice"],
    server_urls = ["https://repo1.maven.org/maven2"],
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
    name = "io_grpc_grpc_stub",
    artifact = "io.grpc:grpc-stub:%s" % GRPC_JAVA_VERSION,
    artifact_sha256 = "65b01e451013d6c9f2de1392abf47190a397cbbd7f5a45e3cc9df509671a0cf8",
    licenses = ["notice"],
    server_urls = ["https://repo1.maven.org/maven2"],
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
    artifact = "com.google.api.grpc:proto-google-common-protos:1.16.0",
    artifact_sha256 = "e6eff21b0a5cc049b0bf2c571fac23abe8dd9d5f9143189f501c04164dc37da2",
    licenses = ["notice"],
    server_urls = ["https://repo1.maven.org/maven2"],
)

jvm_maven_import_external(
    name = "com_google_protobuf_protobuf_java",  # Do not use, but do not remove
    artifact = "com.google.protobuf:protobuf-java:3.5.1",
    artifact_sha256 = "b5e2d91812d183c9f053ffeebcbcda034d4de6679521940a19064714966c2cd4",
    server_urls = MAVEN_REPOS,
)

jvm_maven_import_external(
    name = "com_google_protobuf_protobuf_java_util",
    artifact = "com.google.protobuf:protobuf-java-util:%s" % PROTOBUF_VERSION,
    artifact_sha256 = "5650c66dc2c617fd0b8f58e148b42a6c6a1f57ddbef4cbe86302aefff99ea025",
    licenses = ["notice"],
    server_urls = ["https://repo1.maven.org/maven2"],
)

jvm_maven_import_external(
    name = "failsafe",
    artifact = "net.jodah:failsafe:2.3.1",
    artifact_sha256 = "82d2a9a491e9d6b98f0d45f6fcd2c21bf609694fe1e49b19cc032b751c8e9b74",
    licenses = ["notice"],
    server_urls = ["https://repo1.maven.org/maven2"],
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

jvm_maven_import_external(
    name = "io_grpc_grpc_protobuf",
    artifact = "io.grpc:grpc-protobuf:%s" % GRPC_JAVA_VERSION,
    artifact_sha256 = "ee8cef64c7e10dd373aabd3a4b2ec4878e6d5b3ba43cbf55f3876ddaa79266ea",
    licenses = ["notice"],
    server_urls = ["https://repo1.maven.org/maven2"],
)

jvm_maven_import_external(
    name = "io_grpc_grpc_testing",
    artifact = "io.grpc:grpc-testing:%s" % GRPC_JAVA_VERSION,
    artifact_sha256 = "63449d55902efd7d73dccd84f7c7e77c9c727d5c6e798ed98ca19d36f07d38a5",
    licenses = ["notice"],
    server_urls = ["https://repo1.maven.org/maven2"],
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
        "com.google.guava:guava:25.1-jre",
        "com.google.inject:guice:4.2.2",
        "com.googlecode.gettext-commons:gettext-commons:0.9.8",
        "com.googlecode.libphonenumber:libphonenumber:5.7",
        "com.sun.jersey:jersey-client:1.18.1",
        "com.sun.jersey:jersey-core:1.18.1",
        "com.sun.jersey:jersey-server:1.18.1",
        "com.sun.jersey:jersey-servlet:1.18.1",
        "com.sun.xml.bind:jaxb-impl:3.0.0",
        "com.uber.nullaway:nullaway:0.7.6",
        "commons-beanutils:commons-beanutils:1.9.4",
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
        "io.grpc:grpc-api:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-auth:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-core:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-netty:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-netty-shaded:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-protobuf-lite:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-protobuf-nano:%s" % GRPC_JAVA_NANO_VERSION,
        "io.grpc:grpc-protobuf:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-services:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-stub:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-testing-proto:1.24.0",
        "io.grpc:grpc-testing:%s" % GRPC_JAVA_VERSION,
        "io.jaegertracing:jaeger-core:1.0.0",
        "io.jaegertracing:jaeger-thrift:1.0.0",
        "io.netty:netty-buffer:4.1.50.Final",
        "io.netty:netty-codec-dns:4.1.50.Final",
        "io.netty:netty-codec-http2:4.1.50.Final",
        "io.netty:netty-codec-socks:4.1.50.Final",
        "io.netty:netty-codec:4.1.50.Final",
        "io.netty:netty-common:4.1.50.Final",
        "io.netty:netty-handler:4.1.50.Final",
        "io.netty:netty-resolver:4.1.50.Final",
        "io.netty:netty-resolver-dns:4.1.50.Final",
        "io.netty:netty-tcnative-boringssl-static:%s" % IO_NETTY_BORINGSSL_VERSION,
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
        "io.prometheus:simpleclient_jetty:0.6.0",
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
        "net.jodah:failsafe:2.4.0",
        "net.sourceforge.argparse4j:argparse4j:0.4.3",
        "no.finn.unleash:unleash-client-java:4.1.0",
        "org.apache.commons:commons-lang3:3.9",
        "org.apache.curator:curator-client:4.3.0",
        "org.apache.curator:curator-framework:4.3.0",
        "org.apache.curator:curator-recipes:4.3.0",
        "org.apache.httpcomponents:httpclient:4.5.12",
        "org.apache.httpcomponents:httpcore:4.4.13",
        "org.apache.httpcomponents.client5:httpclient5:5.0.1",
        "org.apache.httpcomponents.client5:httpclient5-fluent:5.0.1",
        "org.apache.httpcomponents.core5:httpcore5:5.0.1",
        "org.apache.kafka:kafka-clients:2.3.1",
        "org.apache.kafka:kafka-streams:2.3.1",
        "org.apache.logging.log4j:log4j-api:2.17.1",
        "org.apache.logging.log4j:log4j-core:2.17.1",
        "org.apache.thrift:libthrift:0.12.0",
        "org.apache.zookeeper:zookeeper:3.5.9",
        "org.apache.zookeeper:zookeeper-jute:3.5.9",
        "com.squareup.okhttp3:okhttp:4.9.0",
        "com.squareup.okhttp3:okhttp-tls:4.9.0",
        "com.squareup.okhttp3:mockwebserver:4.9.0",
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
        "org.glassfish.web:javax.el:2.2.6",
        "org.glassfish.jaxb:jaxb-runtime:2.3.3",
        "org.iban4j:iban4j:3.1.0",
        "org.reflections:reflections:0.9.11",
        "org.slf4j:slf4j-api:1.7.30",
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
        # This is necessary to make sure that we're not transiently depending
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
        "com.google.http-client:google-http-client",
        "com.google.instrumentation:instrumentation-api",
        "com.google.protobuf:protobuf-java-util",
        "com.google.protobuf:protobuf-java",
        "com.googlecode.concurrent-trees:concurrent-trees",
        "com.googlecode.concurrentlinkedhashmap:concurrentlinkedhashmap-lru",
        "de.grundid.opendatalab:geojson-jackson",
        "eu.geekplace.javapinning:java-pinning-jar",
        "hsqldb:hsqldb",
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
    fetch_sources = True,
    maven_install_json = "//third_party/maven:maven_install.json",
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
        "io.dropwizard:dropwizard-configuration": "@aggregation//:io_dropwizard_dropwizard_configuration",
        "io.dropwizard:dropwizard-core": "@aggregation//:io_dropwizard_dropwizard_core",
        "io.dropwizard:dropwizard-jackson": "@aggregation//:io_dropwizard_dropwizard_jackson",
        "io.dropwizard:dropwizard-jetty": "@aggregation//:io_dropwizard_dropwizard_jetty",
        "io.dropwizard:dropwizard-lifecycle": "@aggregation//:io_dropwizard_dropwizard_lifecycle",
        "io.dropwizard:dropwizard-logging": "@aggregation//:io_dropwizard_dropwizard_logging",
        "io.dropwizard:dropwizard-metrics": "@aggregation//:io_dropwizard_dropwizard_metrics",
        "io.dropwizard:dropwizard-servlets": "@aggregation//:io_dropwizard_dropwizard_servlets",
        "io.dropwizard:dropwizard-util": "@aggregation//:io_dropwizard_dropwizard_util",
        "io.dropwizard:dropwizard-validation": "@aggregation//:io_dropwizard_dropwizard_validation",
        "io.grpc:grpc-api": "@grpc_libraries//:io_grpc_grpc_api",
        "io.grpc:grpc-auth": "@grpc_libraries//:io_grpc_grpc_auth",
        "io.grpc:grpc-core": "@grpc_libraries//:io_grpc_grpc_core",
        "io.grpc:grpc-netty": "@grpc_libraries//:io_grpc_grpc_netty",
        "io.grpc:grpc-protobuf": "@grpc_libraries//:io_grpc_grpc_protobuf",
        "io.grpc:grpc-protobuf-lite": "@grpc_libraries//:io_grpc_grpc_protobuf_lite",
        "io.grpc:grpc-protobuf-nano": "@grpc_libraries//:io_grpc_grpc_protobuf_nano",
        "io.grpc:grpc-services": "@grpc_libraries//:io_grpc_grpc_services",
        "io.grpc:grpc-stub": "@grpc_libraries//:io_grpc_grpc_stub",
        "io.grpc:grpc-testing": "@grpc_libraries//:io_grpc_grpc_testing",
        "io.grpc:grpc-testing-proto": "@grpc_libraries//:io_grpc_grpc_testing_proto",
        "io.opencensus:opencensus-api": "@aggregation//:io_opencensus_opencensus_api",
        "io.opencensus:opencensus-contrib-grpc-metrics": "@aggregation//:io_opencensus_opencensus_contrib_grpc_metrics",
        "org.apache.curator:curator-client": "@aggregation//:org_apache_curator_curator_client",
        "org.apache.curator:curator-framework": "@aggregation//:org_apache_curator_curator_framework",
        "org.apache.curator:curator-recipes": "@aggregation//:org_apache_curator_curator_recipes",
        "org.apache.curator:curator-x-discovery": "@aggregation//:org_apache_curator_curator_x_discovery",
        "org.apache.zookeeper:zookeeper": "@aggregation//:org_apache_zookeeper_zookeeper",
        "org.apache.zookeeper:zookeeper-jute": "@aggregation//:org_apache_zookeeper_zookeeper_jute",
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

SPRING_FRAMEWORK_VERSION = "5.2.8.RELEASE"

SPRING_SECURITY_VERSION = "5.3.4.RELEASE"

SPRING_BOOT_VERSION = "2.3.3.RELEASE"

JACKSON_VERSION = "2.11.2"

SLF4J_VERSION = "1.8.0-beta4"

NETTY_VERSION = "4.1.51.Final"

MYSQL_CONNECTOR_VERSION = "5.1.49"

NETTY_BORINGSSL_VERSION = "2.0.30.Final"

BOUNCYCASTLE_VERSION = "1.68"

maven_install(
    name = "agents_platform_maven",
    artifacts = [
        "software.amazon.awssdk:auth:2.7.5",
        "software.amazon.awssdk:aws-core:2.7.5",
        "software.amazon.awssdk:aws-query-protocol:jar:2.7.5",
        "software.amazon.awssdk:aws-xml-protocol:jar:2.7.5",
        "software.amazon.awssdk:http-client-spi:2.7.5",
        "software.amazon.awssdk:netty-nio-client:2.7.5",
        "software.amazon.awssdk:profiles:2.7.5",
        "software.amazon.awssdk:protocol-core:2.7.5",
        "software.amazon.awssdk:regions:2.7.5",
        "software.amazon.awssdk:s3:jar:2.7.5",
        "software.amazon.awssdk:sdk-core:2.7.5",
        "software.amazon.awssdk:utils:2.7.5",
        "ch.qos.logback.contrib:logback-jackson:0.1.5",
        "ch.qos.logback.contrib:logback-json-classic:0.1.5",
        "ch.qos.logback.contrib:logback-json-core:0.1.5",
        "ch.qos.logback:logback-classic:1.2.3",
        "ch.qos.logback:logback-core:1.2.3",
        "com.amazonaws:amazon-sqs-java-messaging-lib:1.0.6",
        "com.amazonaws:aws-java-sdk-sqs:1.11.791",
        "com.fasterxml.jackson.core:jackson-annotations:%s" % JACKSON_VERSION,
        "com.fasterxml.jackson.core:jackson-core:%s" % JACKSON_VERSION,
        "com.fasterxml.jackson.core:jackson-databind:%s" % JACKSON_VERSION,
        "com.github.spotbugs:spotbugs:pom:4.1.1",
        "com.github.tomakehurst:wiremock-standalone:2.27.1",
        "com.google.collections:google-collections:1.0",
        "com.google.guava:guava:28.2-jre",
        "com.puppycrawl.tools:checkstyle:8.29",
        "com.tngtech.archunit:archunit-junit4:0.14.1",
        "com.tngtech.archunit:archunit:0.14.1",
        "commons-codec:commons-codec:1.11",
        "commons-io:commons-io:2.6",
        "io.grpc:grpc-api:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-core:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-netty:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-protobuf-lite:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-protobuf:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-stub:%s" % GRPC_JAVA_VERSION,
        "io.grpc:grpc-testing:%s" % GRPC_JAVA_VERSION,
        "io.micrometer:micrometer-registry-prometheus:1.5.2",
        "io.netty:netty-buffer:%s" % NETTY_VERSION,
        "io.netty:netty-codec:%s" % NETTY_VERSION,
        "io.netty:netty-common:%s" % NETTY_VERSION,
        "io.netty:netty-handler:%s" % NETTY_VERSION,
        "io.netty:netty-resolver:%s" % NETTY_VERSION,
        "io.netty:netty-tcnative-boringssl-static:%s" % NETTY_BORINGSSL_VERSION,
        "io.netty:netty-transport:%s" % NETTY_VERSION,
        "io.prometheus:simpleclient_logback:0.9.0",
        "io.vavr:vavr:0.10.3",
        "jakarta.xml.bind:jakarta.xml.bind-api:2.3.3",
        "javax.annotation:javax.annotation-api:1.3.2",
        "javax.servlet:javax.servlet-api:4.0.1",
        "javax.ws.rs:javax.ws.rs-api:2.1.1",
        "mysql:mysql-connector-java:%s" % MYSQL_CONNECTOR_VERSION,
        "net.sourceforge.pmd:pmd-dist:6.20.0",
        "org.apache.commons:commons-collections4:4.4",
        "org.apache.commons:commons-lang3:3.11",
        "org.apache.httpcomponents:httpclient:4.5.12",
        "org.apache.httpcomponents:httpcore:4.4.13",
        "org.apache.tomcat.embed:tomcat-embed-core:9.0.37",
        "org.aspectj:aspectjweaver:1.9.6",
        "org.assertj:assertj-core:2.9.1",
        "org.bouncycastle:bcpkix-jdk15on:%s" % BOUNCYCASTLE_VERSION,
        "org.bouncycastle:bcprov-jdk15on:%s" % BOUNCYCASTLE_VERSION,
        "org.eclipse.jetty:jetty-server:9.4.30.v20200611",
        "org.eclipse.jetty:jetty-servlets:9.4.30.v20200611",
        "org.eclipse.jetty:jetty-webapp:9.4.30.v20200611",
        "org.glassfish.jersey.core:jersey-common:2.30.1",
        "org.hamcrest:hamcrest-library:1.3",
        "org.junit.jupiter:junit-jupiter:5.6.0",
        "org.mockito:mockito-core:3.2.4",
        "org.slf4j:jcl-over-slf4j:%s" % SLF4J_VERSION,
        "org.slf4j:jul-to-slf4j:%s" % SLF4J_VERSION,
        "org.slf4j:log4j-over-slf4j:%s" % SLF4J_VERSION,
        "org.slf4j:slf4j-api:%s" % SLF4J_VERSION,
        "org.springframework.boot:spring-boot-actuator:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-autoconfigure:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-starter-actuator:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-starter-data-jpa:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-starter-data-redis:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-starter-data-rest:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-starter-security:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-starter-web:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-test-autoconfigure:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-test:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot:%s" % SPRING_BOOT_VERSION,
        "org.springframework.cloud:spring-cloud-aws-messaging:2.1.1.RELEASE",
        "org.springframework.cloud:spring-cloud-starter-aws-messaging:2.1.1.RELEASE",
        "org.springframework.retry:spring-retry:jar:1.2.5.RELEASE",
        "org.springframework.security:spring-security-config:%s" % SPRING_SECURITY_VERSION,
        "org.springframework.security:spring-security-core:%s" % SPRING_SECURITY_VERSION,
        "org.springframework.security:spring-security-test:%s" % SPRING_SECURITY_VERSION,
        "org.springframework.security:spring-security-web:%s" % SPRING_SECURITY_VERSION,
        "org.springframework:spring-aop:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-beans:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-context:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-expression:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-jms:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-messaging:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-test:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-web:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-webmvc:%s" % SPRING_FRAMEWORK_VERSION,
        "org.testcontainers:localstack:1.15.1",
        "org.testcontainers:mysql:1.15.1",
        "org.testcontainers:testcontainers:1.15.1",
        "redis.clients:jedis:3.3.0",
        "net.minidev:json-smart:2.3",
        "com.nimbusds:nimbus-jose-jwt:8.20.1",
        "com.nimbusds:oauth2-oidc-sdk:8.32",
        "org.apache.logging.log4j:log4j-to-slf4j:2.17.1",
        "org.apache.logging.log4j:log4j-api:2.17.1",
    ],
    fetch_sources = True,
    maven_install_json = "//third_party/agents_platform_maven:agents_platform_maven_install.json",
    repositories = [
        "https://repo1.maven.org/maven2",
        "https://jcenter.bintray.com",
    ],
)

load("@agents_platform_maven//:defs.bzl", pinned_agents_platform_maven_install = "pinned_maven_install")

pinned_agents_platform_maven_install()

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

ERROR_PRONE_VERSION = "2.3.3"

AUTO_SERVICE_VERSION = "1.0-rc7"

maven_install(
    name = "aggregation",
    artifacts = [
        "antlr:antlr:2.7.6",
        "aopalliance:aopalliance:1.0",
        "asm:asm:3.3.1",
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
        maven.artifact(
            group = "com.google.errorprone",
            artifact = "error_prone_core",
            version = ERROR_PRONE_VERSION,
            exclusions = [
                "com.google.guava:guava",
                "org.checkerframework:checker-qual",
            ],
        ),
        maven.artifact(
            group = "com.google.errorprone",
            artifact = "error_prone_check_api",
            version = ERROR_PRONE_VERSION,
            exclusions = [
                "com.google.guava:guava",
                "org.checkerframework:checker-qual",
            ],
        ),
        maven.artifact(
            group = "com.google.errorprone",
            artifact = "error_prone_annotations",
            version = ERROR_PRONE_VERSION,
            exclusions = [
                "com.google.guava:guava",
            ],
        ),
        maven.artifact(
            group = "com.google.errorprone",
            artifact = "error_prone_test_helpers",
            version = ERROR_PRONE_VERSION,
            exclusions = [
                "com.google.guava:guava",
                "org.checkerframework:checker-qual",
            ],
        ),
        maven.artifact(
            group = "com.google.errorprone",
            artifact = "javac",
            version = "9+181-r4173-1",
            exclusions = [
                "com.google.guava:guava",
            ],
        ),
        maven.artifact(
            group = "com.google.auto.service",
            artifact = "auto-service",
            version = AUTO_SERVICE_VERSION,
            exclusions = [
                "com.google.guava:guava",
            ],
        ),
        "com.google.auto.service:auto-service-annotations:%s" % AUTO_SERVICE_VERSION,
        "com.google.guava:guava:25.1-jre",
        "com.google.http-client:google-http-client:1.17.0-rc",
        "com.google.inject.extensions:guice-assistedinject:4.2.2",
        "com.google.inject.extensions:guice-grapher:4.2.2",
        "com.google.inject.extensions:guice-multibindings:4.2.2",
        "com.google.inject:guice:4.2.2",
        "com.google.instrumentation:instrumentation-api:0.4.3",
        "com.google.protobuf:protobuf-java-util:3.5.1",
        "com.google.protobuf:protobuf-java:3.5.1",
        "com.google.zxing:core:3.4.1",
        "com.google.zxing:javase:3.4.1",
        "com.googlecode.gettext-commons:gettext-commons:0.9.8",
        "com.googlecode.libphonenumber:libphonenumber:5.7",
        "com.jayway.jsonpath:json-path:2.4.0",
        "com.jcraft:jzlib:1.1.3",
        "com.kjetland:mbknor-jackson-jsonschema_2.12:1.0.34",
        "com.lambdaworks:scrypt:1.3.2",
        "com.mchange:c3p0:0.9.5.4",
        "com.netflix.governator:governator-api:1.17.2",
        "com.netflix.governator:governator-core:1.17.2",
        "com.netflix.governator:governator:1.17.2",
        "com.nimbusds:nimbus-jose-jwt:8.20.1",
        "com.nimbusds:srp6a:2.0.2",
        "com.opsgenie.integration:sdk-shaded:jar:2.8.2",
        "com.oracle.substratevm:svm:19.0.0",
        "com.squareup.okhttp3:okhttp:4.9.0",
        "com.squareup.okio:okio:2.8.0",
        "com.sun.activation:jakarta.activation:2.0.0",
        "com.sun.istack:istack-commons-runtime:3.0.11",
        "com.sun.jersey.contribs:jersey-apache-client4:1.18.1",
        "com.sun.jersey:jersey-client:1.18.1",
        "com.sun.jersey:jersey-core:1.18.1",
        "com.sun.jersey:jersey-server:1.18.1",
        "com.sun.jersey:jersey-servlet:1.18.1",
        "com.sun.xml.bind:jaxb-impl:3.0.0",
        "com.sun.xml.messaging.saaj:saaj-impl:2.0.0",
        "com.sun.xml.ws:jaxws-ri:2.3.3",
        "com.sun.xml.ws:jaxws-rt:2.3.3",
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
        "io.netty:netty-buffer:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
        "io.netty:netty-codec-dns:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
        "io.netty:netty-codec-http2:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
        "io.netty:netty-codec-http:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
        "io.netty:netty-codec-socks:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
        "io.netty:netty-codec:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
        "io.netty:netty-common:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
        "io.netty:netty-dev-tools:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
        "io.netty:netty-handler-proxy:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
        "io.netty:netty-handler:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
        "io.netty:netty-resolver-dns:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
        "io.netty:netty-resolver:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
        "io.netty:netty-tcnative-boringssl-static:%s" % IO_NETTY_BORINGSSL_VERSION,
        "io.netty:netty-transport:4.1.50.Final",  # Should be equal to IO_NETTY_VERSION
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
        "jakarta.xml.bind:jakarta.xml.bind-api:3.0.0",
        "jakarta.xml.soap:jakarta.xml.soap-api:2.0.0",
        "jakarta.xml.ws:jakarta.xml.ws-api:3.0.0",
        "javax.annotation:javax.annotation-api:1.3.2",
        "javax.el:javax.el-api:2.2.5",
        "javax.inject:javax.inject:1",
        "javax.servlet:javax.servlet-api:4.0.1",
        "javax.transaction:jta:1.1",
        "javax.validation:validation-api:2.0.1.Final",
        "javax.xml.bind:jaxb-api:2.3.1",
        "javax.xml.stream:stax-api:1.0-2",
        "javax.xml.soap:saaj-api:1.3.5",
        "javax.xml.ws:jaxws-api:2.3.1",
        "jline:jline:0.9.94",
        "joda-time:joda-time:2.9.9",
        "junit:junit:4.13.2",
        "mysql:mysql-connector-java:5.1.42",
        "net.bytebuddy:byte-buddy-agent:1.11.0",
        "net.bytebuddy:byte-buddy:1.11.0",
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
        maven.artifact(
            group = "org.apache.curator",
            artifact = "curator-client",
            version = "4.3.0",
            exclusions = [
                "com.google.guava:guava",
            ],
        ),
        maven.artifact(
            group = "org.apache.curator",
            artifact = "curator-framework",
            version = "4.3.0",
            exclusions = [
                "com.google.guava:guava",
            ],
        ),
        maven.artifact(
            group = "org.apache.curator",
            artifact = "curator-recipes",
            version = "4.3.0",
            exclusions = [
                "com.google.guava:guava",
            ],
        ),
        maven.artifact(
            group = "org.apache.curator",
            artifact = "curator-x-discovery",
            version = "4.3.0",
            exclusions = [
                "com.google.guava:guava",
            ],
        ),
        "org.apache.httpcomponents:httpclient:4.5.12",
        "org.apache.httpcomponents:httpcore:4.4.13",
        "org.apache.httpcomponents:httpmime:4.5.11",
        "org.apache.logging.log4j:log4j-api:2.17.1",
        "org.apache.logging.log4j:log4j-core:2.17.1",
        "org.apache.mahout.commons:commons-cli:2.0-mahout",
        "org.apache.pdfbox:fontbox:2.0.23",
        "org.apache.pdfbox:pdfbox:2.0.23",
        "org.apache.zookeeper:zookeeper-jute:3.5.9",
        "org.apache.zookeeper:zookeeper:3.5.9",
        "org.aspectj:aspectjrt:1.8.10",
        "org.assertj:assertj-core:3.19.0",
        "org.bitbucket.b_c:jose4j:0.6.5",
        "org.bouncycastle:bcpkix-jdk15on:1.68",
        "org.bouncycastle:bcprov-jdk15on:1.68",
        "org.codehaus.mojo:animal-sniffer-annotations:1.18",
        "org.codehaus.plexus:plexus-utils:3.0.17",
        "org.codehaus.woodstox:stax2-api:4.2.1",
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
        "org.glassfish.jaxb:jaxb-runtime:2.3.3",
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
        "org.mockito:mockito-core:3.10.0",
        "org.modelmapper:modelmapper:2.4.3",
        "org.mozilla:rhino:1.7R4",
        "org.objenesis:objenesis:3.2",
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
        "org.springframework.data:spring-data-jpa:1.11.22.RELEASE",
        "org.springframework.security:spring-security-core:4.2.13.RELEASE",
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
        "org.yaml:snakeyaml:1.26",
        "pl.pragmatists:JUnitParams:1.0.5",
        "software.amazon.ion:ion-java:1.0.2",
        "xerces:xercesImpl:2.12.0",
        "xml-apis:xml-apis:1.4.01",
    ],
    excluded_artifacts = [
        "org.slf4j:slf4j-log4j12",  # log4j-over-slf4j and slf4j-log4j12 cannot coexist on the classpath
        "javassist:javassist",  # Already covered by the newer org.javassist:javassist
        "com.lowagie:itext",  # Cannot add this one for some reason, but it doesn't seem to be needed anyway
        "log4j:log4j",  # Superseded by Log4J2 (org.apache.logging.log4j:log4j-core)

        # Without excluding them we cannot add com.sun.xml.ws:jaxws-ri
        "com.sun.xml.ws:samples",
        "com.sun.xml.ws:release-documentation",
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
        "com.google.protobuf:protobuf-java:%s" % PROTOBUF_VERSION,
        "com.google.protobuf:protobuf-java-util:%s" % PROTOBUF_VERSION,
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
        "net.bytebuddy:byte-buddy:1.11.0",
        "net.sourceforge.htmlunit:htmlunit:2.37.0",
        "org.apache.httpcomponents:httpclient:4.5.12",
        "org.apache.httpcomponents:httpcore:4.4.13",
        "org.codehaus.mojo:animal-sniffer-annotations:1.18",
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

# Don't include artifacts from this one directly. Instead, include the shaded "//third_party/browserup_proxy_core"
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

# Multirun

http_archive(
    name = "com_github_atlassian_bazel_tools",
    sha256 = "77fb626ebde079270d938fb97f74a63bc09ff251df158d653d6f1bec0bbdd36b",
    strip_prefix = "bazel-tools-9aecaa818002e8d51dca8ccef7a482f8134b5337",
    url = "https://github.com/atlassian/bazel-tools/archive/9aecaa818002e8d51dca8ccef7a482f8134b5337.tar.gz",
)

load("@com_github_atlassian_bazel_tools//multirun:deps.bzl", "multirun_dependencies")

multirun_dependencies()

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

http_file(
    name = "install-info_6.5.0.dfsg.1-4b1_amd64",
    downloaded_file_path = "install-info_6.5.0.dfsg.1-4+b1_amd64.deb",
    sha256 = "1b29164d4254cf34084fcd3b8ee2c4782874386e2a1c5099a1f5b55475052815",
    urls = ["http://deb.debian.org/debian/pool/main/t/texinfo/install-info_6.5.0.dfsg.1-4+b1_amd64.deb"],
)

http_file(
    name = "libapparmor1_2.13.2-10_amd64",
    downloaded_file_path = "libapparmor1_2.13.2-10_amd64.deb",
    sha256 = "2f7811c696db52d44a71a0086becf52aa6232cc8bc829d4a661bae85d0522cb1",
    urls = ["http://deb.debian.org/debian/pool/main/a/apparmor/libapparmor1_2.13.2-10_amd64.deb"],
)

http_file(
    name = "libcap2_2.25-2_amd64",
    downloaded_file_path = "libcap2_2.25-2_amd64.deb",
    sha256 = "8f93459c99e9143dfb458353336c5171276860896fd3e10060a515cd3ea3987b",
    urls = ["http://deb.debian.org/debian/pool/main/libc/libcap2/libcap2_2.25-2_amd64.deb"],
)

http_file(
    name = "libargon2-1_020171227-0.2_amd64",
    downloaded_file_path = "libargon2-1_0~20171227-0.2_amd64.deb",
    sha256 = "0d2be32c122d26bbd9b604fbe0072265e4978e07b0e1b7149ba364ba3cc5a302",
    urls = ["http://deb.debian.org/debian/pool/main/a/argon2/libargon2-1_0~20171227-0.2_amd64.deb"],
)

http_file(
    name = "dmsetup_1.02.155-3_amd64",
    downloaded_file_path = "dmsetup_1.02.155-3_amd64.deb",
    sha256 = "384c761c876cf55bd6cd47f1555e9e866be3b32a569021f7bcc576632cffc42f",
    urls = ["http://deb.debian.org/debian/pool/main/l/lvm2/dmsetup_1.02.155-3_amd64.deb"],
)

http_file(
    name = "libdevmapper1.02.1_1.02.155-3_amd64",
    downloaded_file_path = "libdevmapper1.02.1_1.02.155-3_amd64.deb",
    sha256 = "92ffc2aeb36fbb79992a56354f557041aacaa675112647926aa1031a810d8d31",
    urls = ["http://deb.debian.org/debian/pool/main/l/lvm2/libdevmapper1.02.1_1.02.155-3_amd64.deb"],
)

http_file(
    name = "libjson-c3_0.12.1ds-2deb10u1_amd64",
    downloaded_file_path = "libjson-c3_0.12.1+ds-2+deb10u1_amd64.deb",
    sha256 = "5b010246a415b210cb621a54324534cab5ac2cd94481a80705a0f2c426ae8a92",
    urls = ["http://deb.debian.org/debian/pool/main/j/json-c/libjson-c3_0.12.1+ds-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libcryptsetup12_2.1.0-5deb10u2_amd64",
    downloaded_file_path = "libcryptsetup12_2.1.0-5+deb10u2_amd64.deb",
    sha256 = "abeb8c756d5d4aa3147d115c208d95096ab6f453f2c629575abac7505d5d2bee",
    urls = ["http://deb.debian.org/debian/pool/main/c/cryptsetup/libcryptsetup12_2.1.0-5+deb10u2_amd64.deb"],
)

http_file(
    name = "libidn11_1.33-2.2_amd64",
    downloaded_file_path = "libidn11_1.33-2.2_amd64.deb",
    sha256 = "24c2e0af277992e2e18ac51ac4912427eec0e4cc7b130317ea75a3beec11ef68",
    urls = ["http://deb.debian.org/debian/pool/main/libi/libidn/libidn11_1.33-2.2_amd64.deb"],
)

http_file(
    name = "libip4tc0_1.8.2-4_amd64",
    downloaded_file_path = "libip4tc0_1.8.2-4_amd64.deb",
    sha256 = "5b7dd28d6a34fb9a28d1cf5fa3bb631624396ef7366afdafba6c456c90c99c6e",
    urls = ["http://deb.debian.org/debian/pool/main/i/iptables/libip4tc0_1.8.2-4_amd64.deb"],
)

http_file(
    name = "libkmod2_26-1_amd64",
    downloaded_file_path = "libkmod2_26-1_amd64.deb",
    sha256 = "76c614ad75b5886de0db7ddc1e99f6692faaa8225176a65a54f1c87805337184",
    urls = ["http://deb.debian.org/debian/pool/main/k/kmod/libkmod2_26-1_amd64.deb"],
)

http_file(
    name = "systemd_241-7deb10u8_amd64",
    downloaded_file_path = "systemd_241-7~deb10u8_amd64.deb",
    sha256 = "a60a47ef65cf1d5680555b749a22e679c2889095df10031a6321771e70e9ce4d",
    urls = ["http://deb.debian.org/debian/pool/main/s/systemd/systemd_241-7~deb10u8_amd64.deb"],
)

http_file(
    name = "systemd-sysv_241-7deb10u8_amd64",
    downloaded_file_path = "systemd-sysv_241-7~deb10u8_amd64.deb",
    sha256 = "6a3c84ccfd1ba55257178a6241b020d514ca13b470e7f5381581735da8513606",
    urls = ["http://deb.debian.org/debian/pool/main/s/systemd/systemd-sysv_241-7~deb10u8_amd64.deb"],
)

http_file(
    name = "perl-modules-5.28_5.28.1-6deb10u1_all",
    downloaded_file_path = "perl-modules-5.28_5.28.1-6+deb10u1_all.deb",
    sha256 = "61c00b6f4c7a242aa6aefdc07012747607cd9c6c5392c1dcd087ce27031dd1dd",
    urls = ["http://deb.debian.org/debian/pool/main/p/perl/perl-modules-5.28_5.28.1-6+deb10u1_all.deb"],
)

http_file(
    name = "libgdbm6_1.18.1-4_amd64",
    downloaded_file_path = "libgdbm6_1.18.1-4_amd64.deb",
    sha256 = "d2cf8da8b3d575f0c2d7cb0a687e63b617a7bc85e434b8b1a2abe63481977619",
    urls = ["http://deb.debian.org/debian/pool/main/g/gdbm/libgdbm6_1.18.1-4_amd64.deb"],
)

http_file(
    name = "libgdbm-compat4_1.18.1-4_amd64",
    downloaded_file_path = "libgdbm-compat4_1.18.1-4_amd64.deb",
    sha256 = "5af8fd893c3b065f46365efa3f037c8ac22af00683fed15b1ea36970bbabd764",
    urls = ["http://deb.debian.org/debian/pool/main/g/gdbm/libgdbm-compat4_1.18.1-4_amd64.deb"],
)

http_file(
    name = "libperl5.28_5.28.1-6deb10u1_amd64",
    downloaded_file_path = "libperl5.28_5.28.1-6+deb10u1_amd64.deb",
    sha256 = "0ca12c0fe7534a5a08df265b773b6a15a9704db008c15f366dc41cd4be53a2ad",
    urls = ["http://deb.debian.org/debian/pool/main/p/perl/libperl5.28_5.28.1-6+deb10u1_amd64.deb"],
)

http_file(
    name = "perl_5.28.1-6deb10u1_amd64",
    downloaded_file_path = "perl_5.28.1-6+deb10u1_amd64.deb",
    sha256 = "0b7a8c30b3511f5a0de38a4300a14ccb8a9e304a3e96c0aa105e12cb4033562c",
    urls = ["http://deb.debian.org/debian/pool/main/p/perl/perl_5.28.1-6+deb10u1_amd64.deb"],
)

http_file(
    name = "libpython3.7-minimal_3.7.3-2deb10u3_amd64",
    downloaded_file_path = "libpython3.7-minimal_3.7.3-2+deb10u3_amd64.deb",
    sha256 = "9135e295b3710c0c3fd3fbc1ba57e3925294bbf2d6cc27a42bd382dd905a044f",
    urls = ["http://deb.debian.org/debian/pool/main/p/python3.7/libpython3.7-minimal_3.7.3-2+deb10u3_amd64.deb"],
)

http_file(
    name = "python3.7-minimal_3.7.3-2deb10u3_amd64",
    downloaded_file_path = "python3.7-minimal_3.7.3-2+deb10u3_amd64.deb",
    sha256 = "496a2fff86a2433e26b4c0736ea762426dee1eb0b7578951e0d3392aa6e242b4",
    urls = ["http://deb.debian.org/debian/pool/main/p/python3.7/python3.7-minimal_3.7.3-2+deb10u3_amd64.deb"],
)

http_file(
    name = "python3-minimal_3.7.3-1_amd64",
    downloaded_file_path = "python3-minimal_3.7.3-1_amd64.deb",
    sha256 = "9c937923b35ac24f5cb6be81626f00dd6b810fc0889e5b3b08b7ffc9d179ff1b",
    urls = ["http://deb.debian.org/debian/pool/main/p/python3-defaults/python3-minimal_3.7.3-1_amd64.deb"],
)

http_file(
    name = "mime-support_3.62_all",
    downloaded_file_path = "mime-support_3.62_all.deb",
    sha256 = "776efd686af26fa26325450280e3305463b1faef75d82b383bb00da61893d8ca",
    urls = ["http://deb.debian.org/debian/pool/main/m/mime-support/mime-support_3.62_all.deb"],
)

http_file(
    name = "libmpdec2_2.4.2-2_amd64",
    downloaded_file_path = "libmpdec2_2.4.2-2_amd64.deb",
    sha256 = "9ca85e6e2645a5e660431294320658ec7a2910d9fed90ca4e648c1211a2b844b",
    urls = ["http://deb.debian.org/debian/pool/main/m/mpdecimal/libmpdec2_2.4.2-2_amd64.deb"],
)

http_file(
    name = "libpython3.7-stdlib_3.7.3-2deb10u3_amd64",
    downloaded_file_path = "libpython3.7-stdlib_3.7.3-2+deb10u3_amd64.deb",
    sha256 = "36b978fcacde4e5cddc205e32b7d5c50525fef83481144ac9ec4cc83ed9e213a",
    urls = ["http://deb.debian.org/debian/pool/main/p/python3.7/libpython3.7-stdlib_3.7.3-2+deb10u3_amd64.deb"],
)

http_file(
    name = "python3.7_3.7.3-2deb10u3_amd64",
    downloaded_file_path = "python3.7_3.7.3-2+deb10u3_amd64.deb",
    sha256 = "daeb04ca12b6d72cb12aed87d9ecdf1bc302bebfde1206041ebe5531b404a4e7",
    urls = ["http://deb.debian.org/debian/pool/main/p/python3.7/python3.7_3.7.3-2+deb10u3_amd64.deb"],
)

http_file(
    name = "libpython3-stdlib_3.7.3-1_amd64",
    downloaded_file_path = "libpython3-stdlib_3.7.3-1_amd64.deb",
    sha256 = "4f8883d378e698aa89b7bd4b68ce8e7cca01c961d3df87fafe4c079bb4668f5b",
    urls = ["http://deb.debian.org/debian/pool/main/p/python3-defaults/libpython3-stdlib_3.7.3-1_amd64.deb"],
)

http_file(
    name = "python3_3.7.3-1_amd64",
    downloaded_file_path = "python3_3.7.3-1_amd64.deb",
    sha256 = "eb7862c7ad2cf5b86f3851c7103f72f8fa45b48514ddcf371a8e4ba8f02a79e5",
    urls = ["http://deb.debian.org/debian/pool/main/p/python3-defaults/python3_3.7.3-1_amd64.deb"],
)

http_file(
    name = "libdebian-installer4_0.119_amd64",
    downloaded_file_path = "libdebian-installer4_0.119_amd64.deb",
    sha256 = "73c55799ab82ad9a4dcc50e41de31a1dd7b859b1603e524420305c52b3eead92",
    urls = ["http://deb.debian.org/debian/pool/main/libd/libdebian-installer/libdebian-installer4_0.119_amd64.deb"],
)

http_file(
    name = "libslang2_2.3.2-2_amd64",
    downloaded_file_path = "libslang2_2.3.2-2_amd64.deb",
    sha256 = "d94c51ea5cdf253019b67867bf4b0a5116ab224e97fd767614f0af31c63477bd",
    urls = ["http://deb.debian.org/debian/pool/main/s/slang2/libslang2_2.3.2-2_amd64.deb"],
)

http_file(
    name = "libnewt0.52_0.52.20-8_amd64",
    downloaded_file_path = "libnewt0.52_0.52.20-8_amd64.deb",
    sha256 = "0f1ea5e21092a9d742a48ce437638da89bdfaee623ce82b470d6a2876035b26f",
    urls = ["http://deb.debian.org/debian/pool/main/n/newt/libnewt0.52_0.52.20-8_amd64.deb"],
)

http_file(
    name = "libtextwrap1_0.1-14.2_amd64",
    downloaded_file_path = "libtextwrap1_0.1-14.2_amd64.deb",
    sha256 = "6626eee49a3ad10c596955f1180bee6c937f5e9ea1404085516a29010ab8bd23",
    urls = ["http://deb.debian.org/debian/pool/main/libt/libtextwrap/libtextwrap1_0.1-14.2_amd64.deb"],
)

http_file(
    name = "cdebconf_0.249_amd64",
    downloaded_file_path = "cdebconf_0.249_amd64.deb",
    sha256 = "0dfda2da67a30ef5abc7d18e0b93045aec38ab2e53d70735f2d979352ffbe8ba",
    urls = ["http://deb.debian.org/debian/pool/main/c/cdebconf/cdebconf_0.249_amd64.deb"],
)

http_file(
    name = "libelf1_0.176-1.1_amd64",
    downloaded_file_path = "libelf1_0.176-1.1_amd64.deb",
    sha256 = "cc7496ca986aa77d01e136b8ded5a3e371ec8f248b331b4124d1fd2cbeaec3ef",
    urls = ["http://deb.debian.org/debian/pool/main/e/elfutils/libelf1_0.176-1.1_amd64.deb"],
)

http_file(
    name = "libmnl0_1.0.4-2_amd64",
    downloaded_file_path = "libmnl0_1.0.4-2_amd64.deb",
    sha256 = "f5e67db76e1e09732cb11d53cad3bdd875154bee9a504055595a27ea579abaa6",
    urls = ["http://deb.debian.org/debian/pool/main/libm/libmnl/libmnl0_1.0.4-2_amd64.deb"],
)

http_file(
    name = "libxtables12_1.8.2-4_amd64",
    downloaded_file_path = "libxtables12_1.8.2-4_amd64.deb",
    sha256 = "67f14da8d3f41a2d8a6446f525c254c1a5e9ae1e83c458d5bb297fa9efeef121",
    urls = ["http://deb.debian.org/debian/pool/main/i/iptables/libxtables12_1.8.2-4_amd64.deb"],
)

http_file(
    name = "libcap2-bin_2.25-2_amd64",
    downloaded_file_path = "libcap2-bin_2.25-2_amd64.deb",
    sha256 = "3c8c5b1410447356125fd8f5af36d0c28853b97c072037af4a1250421008b781",
    urls = ["http://deb.debian.org/debian/pool/main/libc/libcap2/libcap2-bin_2.25-2_amd64.deb"],
)

http_file(
    name = "iproute2_4.20.0-2deb10u1_amd64",
    downloaded_file_path = "iproute2_4.20.0-2+deb10u1_amd64.deb",
    sha256 = "bcba12719b4938f0e7f615ac470f5d1e3a9453c50c5be4395fc16e0f337a5144",
    urls = ["http://deb.debian.org/debian/pool/main/i/iproute2/iproute2_4.20.0-2+deb10u1_amd64.deb"],
)

http_file(
    name = "netbase_5.6_all",
    downloaded_file_path = "netbase_5.6_all.deb",
    sha256 = "baf0872964df0ccb10e464b47d995acbba5a0d12a97afe2646d9a6bb97e8d79d",
    urls = ["http://deb.debian.org/debian/pool/main/n/netbase/netbase_5.6_all.deb"],
)

http_file(
    name = "libdbus-1-3_1.12.20-0deb10u1_amd64",
    downloaded_file_path = "libdbus-1-3_1.12.20-0+deb10u1_amd64.deb",
    sha256 = "e394bd35626e3ccf437e1e7776e6573636e6413b0ebe2483bd54ac243eed1007",
    urls = ["http://deb.debian.org/debian/pool/main/d/dbus/libdbus-1-3_1.12.20-0+deb10u1_amd64.deb"],
)

http_file(
    name = "dbus_1.12.20-0deb10u1_amd64",
    downloaded_file_path = "dbus_1.12.20-0+deb10u1_amd64.deb",
    sha256 = "d732c79ed5b9a651d5d83cb015eabdd353b639bd36fd87e9509699e35dc6dcd6",
    urls = ["http://deb.debian.org/debian/pool/main/d/dbus/dbus_1.12.20-0+deb10u1_amd64.deb"],
)

http_file(
    name = "libmagic-mgc_5.35-4deb10u2_amd64",
    downloaded_file_path = "libmagic-mgc_5.35-4+deb10u2_amd64.deb",
    sha256 = "4b685fb59cb69a276b8f03035c0cc86c45e78c956aa84b2a3263cde0229edfca",
    urls = ["http://deb.debian.org/debian/pool/main/f/file/libmagic-mgc_5.35-4+deb10u2_amd64.deb"],
)

http_file(
    name = "libmagic1_5.35-4deb10u2_amd64",
    downloaded_file_path = "libmagic1_5.35-4+deb10u2_amd64.deb",
    sha256 = "247e6681da3f2062e24b8ac9ebd0c4d6738b40efdf1e69d8adc62c10f05b24e2",
    urls = ["http://deb.debian.org/debian/pool/main/f/file/libmagic1_5.35-4+deb10u2_amd64.deb"],
)

http_file(
    name = "file_5.35-4deb10u2_amd64",
    downloaded_file_path = "file_5.35-4+deb10u2_amd64.deb",
    sha256 = "526541e1e36d4fc042e93ae53bb21425e6bba2821ce09fa6c95119dffc5bf866",
    urls = ["http://deb.debian.org/debian/pool/main/f/file/file_5.35-4+deb10u2_amd64.deb"],
)

http_file(
    name = "krb5-locales_1.17-3deb10u3_all",
    downloaded_file_path = "krb5-locales_1.17-3+deb10u3_all.deb",
    sha256 = "7710d790ca64e06203dd5053bbfd4f6919a3d713ac22b3728ec97b3795024663",
    urls = ["http://deb.debian.org/debian/pool/main/k/krb5/krb5-locales_1.17-3+deb10u3_all.deb"],
)

http_file(
    name = "libnss-systemd_241-7deb10u8_amd64",
    downloaded_file_path = "libnss-systemd_241-7~deb10u8_amd64.deb",
    sha256 = "9dc2600bc59d9945d3a7ac97092654d73c2fa8fd4b903d3a686209840cb0bc80",
    urls = ["http://deb.debian.org/debian/pool/main/s/systemd/libnss-systemd_241-7~deb10u8_amd64.deb"],
)

http_file(
    name = "libpam-systemd_241-7deb10u8_amd64",
    downloaded_file_path = "libpam-systemd_241-7~deb10u8_amd64.deb",
    sha256 = "248bf888c891c5ff55965573141391a92613929ee6afc931ae55a3d0a065c6b3",
    urls = ["http://deb.debian.org/debian/pool/main/s/systemd/libpam-systemd_241-7~deb10u8_amd64.deb"],
)

http_file(
    name = "hicolor-icon-theme_0.17-2_all",
    downloaded_file_path = "hicolor-icon-theme_0.17-2_all.deb",
    sha256 = "20304d34b85a734ec1e4830badf3a3a70a5dc5f9c1afc0b2230ecd760c81b5e0",
    urls = ["http://deb.debian.org/debian/pool/main/h/hicolor-icon-theme/hicolor-icon-theme_0.17-2_all.deb"],
)

http_file(
    name = "libglib2.0-0_2.58.3-2deb10u3_amd64",
    downloaded_file_path = "libglib2.0-0_2.58.3-2+deb10u3_amd64.deb",
    sha256 = "566e664cc69e23fade34be92711f359ec02a9854222244f3603fdf640cc6688a",
    urls = ["http://deb.debian.org/debian/pool/main/g/glib2.0/libglib2.0-0_2.58.3-2+deb10u3_amd64.deb"],
)

http_file(
    name = "libjpeg62-turbo_1.5.2-2deb10u1_amd64",
    downloaded_file_path = "libjpeg62-turbo_1.5.2-2+deb10u1_amd64.deb",
    sha256 = "b6cbc7d722cbf697cedbcd9b8b209f8cfa05f147fba4061adf2fcee6cc64c556",
    urls = ["http://deb.debian.org/debian/pool/main/libj/libjpeg-turbo/libjpeg62-turbo_1.5.2-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libjbig0_2.1-3.1b2_amd64",
    downloaded_file_path = "libjbig0_2.1-3.1+b2_amd64.deb",
    sha256 = "9646d69eefce505407bf0437ea12fb7c2d47a3fd4434720ba46b642b6dcfd80f",
    urls = ["http://deb.debian.org/debian/pool/main/j/jbigkit/libjbig0_2.1-3.1+b2_amd64.deb"],
)

http_file(
    name = "libwebp6_0.6.1-2deb10u1_amd64",
    downloaded_file_path = "libwebp6_0.6.1-2+deb10u1_amd64.deb",
    sha256 = "f4d8e88f87f41530bbe8ad45f60ab87e313ef1ebc9035f9ff24649fc9dd746a9",
    urls = ["http://deb.debian.org/debian/pool/main/libw/libwebp/libwebp6_0.6.1-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libtiff5_4.1.0git191117-2deb10u2_amd64",
    downloaded_file_path = "libtiff5_4.1.0+git191117-2~deb10u2_amd64.deb",
    sha256 = "fc8ee9e6b088cf408c9234e707ed8aa18cbe1e811e93c8a63744bf65be1f7bc9",
    urls = ["http://deb.debian.org/debian/pool/main/t/tiff/libtiff5_4.1.0+git191117-2~deb10u2_amd64.deb"],
)

http_file(
    name = "libxau6_1.0.8-1b2_amd64",
    downloaded_file_path = "libxau6_1.0.8-1+b2_amd64.deb",
    sha256 = "a7857b726c3e0d16cda2fbb9020d42e024a3160d54ef858f58578612276683e8",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxau/libxau6_1.0.8-1+b2_amd64.deb"],
)

http_file(
    name = "libbsd0_0.9.1-2deb10u1_amd64",
    downloaded_file_path = "libbsd0_0.9.1-2+deb10u1_amd64.deb",
    sha256 = "cb94f17522cf81be0c9c69ab7ca52745c70bdbfb4c2ba00d2062c2e9dcfe9c6f",
    urls = ["http://deb.debian.org/debian/pool/main/libb/libbsd/libbsd0_0.9.1-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libxdmcp6_1.1.2-3_amd64",
    downloaded_file_path = "libxdmcp6_1.1.2-3_amd64.deb",
    sha256 = "ecb8536f5fb34543b55bb9dc5f5b14c9dbb4150a7bddb3f2287b7cab6e9d25ef",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxdmcp/libxdmcp6_1.1.2-3_amd64.deb"],
)

http_file(
    name = "libxcb1_1.13.1-2_amd64",
    downloaded_file_path = "libxcb1_1.13.1-2_amd64.deb",
    sha256 = "87d9ed9340dc3cb6d7ce024d2e046a659d91356863083715d2c428a32e908833",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxcb/libxcb1_1.13.1-2_amd64.deb"],
)

http_file(
    name = "libx11-data_1.6.7-1deb10u2_all",
    downloaded_file_path = "libx11-data_1.6.7-1+deb10u2_all.deb",
    sha256 = "2c7473b87466a8eae640e44d80282a2c7474374858031fb1a1d499426b13dd75",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libx11/libx11-data_1.6.7-1+deb10u2_all.deb"],
)

http_file(
    name = "libx11-6_1.6.7-1deb10u2_amd64",
    downloaded_file_path = "libx11-6_1.6.7-1+deb10u2_amd64.deb",
    sha256 = "423d2cfea0a0c19613a5c053cc88b1bf75e6a4b2782ee615a4e6516e0b50a5b6",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libx11/libx11-6_1.6.7-1+deb10u2_amd64.deb"],
)

http_file(
    name = "libicu63_63.1-6deb10u1_amd64",
    downloaded_file_path = "libicu63_63.1-6+deb10u1_amd64.deb",
    sha256 = "603f929d1ae548a8faa1f892ae93e623bde97de7ddbe4a796618c735ca7ff8b8",
    urls = ["http://deb.debian.org/debian/pool/main/i/icu/libicu63_63.1-6+deb10u1_amd64.deb"],
)

http_file(
    name = "libxml2_2.9.4dfsg1-7deb10u2_amd64",
    downloaded_file_path = "libxml2_2.9.4+dfsg1-7+deb10u2_amd64.deb",
    sha256 = "3d8137a458b53eb85c4b7440faf62086b645077e1cfaef94065bd79c18a1118e",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxml2/libxml2_2.9.4+dfsg1-7+deb10u2_amd64.deb"],
)

http_file(
    name = "shared-mime-info_1.10-1_amd64",
    downloaded_file_path = "shared-mime-info_1.10-1_amd64.deb",
    sha256 = "6a19f62c59788ba3a52c8b08750a263edde89ac98e63c7e4ccfb14b40eafaf51",
    urls = ["http://deb.debian.org/debian/pool/main/s/shared-mime-info/shared-mime-info_1.10-1_amd64.deb"],
)

http_file(
    name = "libgdk-pixbuf2.0-common_2.38.1dfsg-1_all",
    downloaded_file_path = "libgdk-pixbuf2.0-common_2.38.1+dfsg-1_all.deb",
    sha256 = "1310e3f0258866eb4d0e95f140d5d9025cf6be1e3e2c375f4a426ccc2e78cf68",
    urls = ["http://deb.debian.org/debian/pool/main/g/gdk-pixbuf/libgdk-pixbuf2.0-common_2.38.1+dfsg-1_all.deb"],
)

http_file(
    name = "libgdk-pixbuf2.0-0_2.38.1dfsg-1_amd64",
    downloaded_file_path = "libgdk-pixbuf2.0-0_2.38.1+dfsg-1_amd64.deb",
    sha256 = "90e1842771968ffae4b4c28f1ad6a8bf77ff3a57616b799abed93354b860edc8",
    urls = ["http://deb.debian.org/debian/pool/main/g/gdk-pixbuf/libgdk-pixbuf2.0-0_2.38.1+dfsg-1_amd64.deb"],
)

http_file(
    name = "gtk-update-icon-cache_3.24.5-1_amd64",
    downloaded_file_path = "gtk-update-icon-cache_3.24.5-1_amd64.deb",
    sha256 = "ca87a8eaa7a662049e2a95f3405d8affb7715a9dbdcba6fa186ae0bcc8981847",
    urls = ["http://deb.debian.org/debian/pool/main/g/gtk+3.0/gtk-update-icon-cache_3.24.5-1_amd64.deb"],
)

http_file(
    name = "libpixman-1-0_0.36.0-1_amd64",
    downloaded_file_path = "libpixman-1-0_0.36.0-1_amd64.deb",
    sha256 = "4382ebfc5c52623d917dc0f63c22fbf7a791d00f5b303cd56a44bf9616fa5fbe",
    urls = ["http://deb.debian.org/debian/pool/main/p/pixman/libpixman-1-0_0.36.0-1_amd64.deb"],
)

http_file(
    name = "libxcb-render0_1.13.1-2_amd64",
    downloaded_file_path = "libxcb-render0_1.13.1-2_amd64.deb",
    sha256 = "7bd78eb3d27de76d43185d68914e938d60f233737f7a05586888072695cab6fb",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxcb/libxcb-render0_1.13.1-2_amd64.deb"],
)

http_file(
    name = "libxcb-shm0_1.13.1-2_amd64",
    downloaded_file_path = "libxcb-shm0_1.13.1-2_amd64.deb",
    sha256 = "a7a9927c9b656c253fe6f61497b94aa7332e2270cc30ca67c2925a3ecb61d742",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxcb/libxcb-shm0_1.13.1-2_amd64.deb"],
)

http_file(
    name = "libxext6_1.3.3-1b2_amd64",
    downloaded_file_path = "libxext6_1.3.3-1+b2_amd64.deb",
    sha256 = "724901105792e983bd0e7c2b46960cd925dd6a2b33b5ee999b4e80aaf624b082",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxext/libxext6_1.3.3-1+b2_amd64.deb"],
)

http_file(
    name = "libxrender1_0.9.10-1_amd64",
    downloaded_file_path = "libxrender1_0.9.10-1_amd64.deb",
    sha256 = "3ea17d07b5aa89012130e2acd92f0fc0ea67314e2f5eab6e33930ef688f48294",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxrender/libxrender1_0.9.10-1_amd64.deb"],
)

http_file(
    name = "libcairo2_1.16.0-4deb10u1_amd64",
    downloaded_file_path = "libcairo2_1.16.0-4+deb10u1_amd64.deb",
    sha256 = "228e8af1f2ea388367c26a64a4491ee3f758d9e61a73f421bb60a03c07b30d2b",
    urls = ["http://deb.debian.org/debian/pool/main/c/cairo/libcairo2_1.16.0-4+deb10u1_amd64.deb"],
)

http_file(
    name = "libcroco3_0.6.12-3_amd64",
    downloaded_file_path = "libcroco3_0.6.12-3_amd64.deb",
    sha256 = "1acb00996b7477687e4f3f12de7fbf4b635866a6167671f2201ea3e67af05336",
    urls = ["http://deb.debian.org/debian/pool/main/libc/libcroco/libcroco3_0.6.12-3_amd64.deb"],
)

http_file(
    name = "libfribidi0_1.0.5-3.1deb10u1_amd64",
    downloaded_file_path = "libfribidi0_1.0.5-3.1+deb10u1_amd64.deb",
    sha256 = "9844b02a3bfa8c9f89a077cc5208122f9245a6a6301cbf5fdc66b1a76f163c08",
    urls = ["http://deb.debian.org/debian/pool/main/f/fribidi/libfribidi0_1.0.5-3.1+deb10u1_amd64.deb"],
)

http_file(
    name = "libthai-data_0.1.28-2_all",
    downloaded_file_path = "libthai-data_0.1.28-2_all.deb",
    sha256 = "267d6b251f77c17fb1415ac0727675cb978c895cc1c77d7540e7133125614366",
    urls = ["http://deb.debian.org/debian/pool/main/libt/libthai/libthai-data_0.1.28-2_all.deb"],
)

http_file(
    name = "libdatrie1_0.2.12-2_amd64",
    downloaded_file_path = "libdatrie1_0.2.12-2_amd64.deb",
    sha256 = "7159a08f4a40f74e4582ebd62db0fb48b3ba8e592655ac2ab44f7bfacbca12f3",
    urls = ["http://deb.debian.org/debian/pool/main/libd/libdatrie/libdatrie1_0.2.12-2_amd64.deb"],
)

http_file(
    name = "libthai0_0.1.28-2_amd64",
    downloaded_file_path = "libthai0_0.1.28-2_amd64.deb",
    sha256 = "40e7fbd1ed27185879836b43fb8a739c8991a6d589fef9fb2b3b63e188a537ae",
    urls = ["http://deb.debian.org/debian/pool/main/libt/libthai/libthai0_0.1.28-2_amd64.deb"],
)

http_file(
    name = "libpango-1.0-0_1.42.4-8deb10u1_amd64",
    downloaded_file_path = "libpango-1.0-0_1.42.4-8~deb10u1_amd64.deb",
    sha256 = "efd677c77cb5e89dd94a6f981c7dd4e705e393b61ba4fd3610009d2ca140fa11",
    urls = ["http://deb.debian.org/debian/pool/main/p/pango1.0/libpango-1.0-0_1.42.4-8~deb10u1_amd64.deb"],
)

http_file(
    name = "libgraphite2-3_1.3.13-7_amd64",
    downloaded_file_path = "libgraphite2-3_1.3.13-7_amd64.deb",
    sha256 = "f79bfdcfe09285cccee68c070171888b98adbf3e7bd3e8f6afcb6caef5623179",
    urls = ["http://deb.debian.org/debian/pool/main/g/graphite2/libgraphite2-3_1.3.13-7_amd64.deb"],
)

http_file(
    name = "libharfbuzz0b_2.3.1-1_amd64",
    downloaded_file_path = "libharfbuzz0b_2.3.1-1_amd64.deb",
    sha256 = "aee1dd6f9884c1acdd1b6d6f49bd419235decd00f49cd927e4be4c37af2ecdab",
    urls = ["http://deb.debian.org/debian/pool/main/h/harfbuzz/libharfbuzz0b_2.3.1-1_amd64.deb"],
)

http_file(
    name = "libpangoft2-1.0-0_1.42.4-8deb10u1_amd64",
    downloaded_file_path = "libpangoft2-1.0-0_1.42.4-8~deb10u1_amd64.deb",
    sha256 = "4724bb2dfa25beb9c1a978de0c5780f6d3ce5aa03232e47c11b297b4899a6073",
    urls = ["http://deb.debian.org/debian/pool/main/p/pango1.0/libpangoft2-1.0-0_1.42.4-8~deb10u1_amd64.deb"],
)

http_file(
    name = "libpangocairo-1.0-0_1.42.4-8deb10u1_amd64",
    downloaded_file_path = "libpangocairo-1.0-0_1.42.4-8~deb10u1_amd64.deb",
    sha256 = "3ca2e8aaa6c061f91ae05163ce31b3bc63a611c63c1a43ccf855330e505825f0",
    urls = ["http://deb.debian.org/debian/pool/main/p/pango1.0/libpangocairo-1.0-0_1.42.4-8~deb10u1_amd64.deb"],
)

http_file(
    name = "librsvg2-2_2.44.10-2.1_amd64",
    downloaded_file_path = "librsvg2-2_2.44.10-2.1_amd64.deb",
    sha256 = "181188485d646e0ac29e79df67d8fa3ca7a984bb65024b06b36e917b4e282e21",
    urls = ["http://deb.debian.org/debian/pool/main/libr/librsvg/librsvg2-2_2.44.10-2.1_amd64.deb"],
)

http_file(
    name = "librsvg2-common_2.44.10-2.1_amd64",
    downloaded_file_path = "librsvg2-common_2.44.10-2.1_amd64.deb",
    sha256 = "c873d99436da50dfcc23104d827bd73e5063d9ee5742f39ffeb44ba1145af5e1",
    urls = ["http://deb.debian.org/debian/pool/main/libr/librsvg/librsvg2-common_2.44.10-2.1_amd64.deb"],
)

http_file(
    name = "adwaita-icon-theme_3.30.1-1_all",
    downloaded_file_path = "adwaita-icon-theme_3.30.1-1_all.deb",
    sha256 = "698b3f0fa337bb36ea4fe072a37a32a1c81875db13042368677490bb087ccb93",
    urls = ["http://deb.debian.org/debian/pool/main/a/adwaita-icon-theme/adwaita-icon-theme_3.30.1-1_all.deb"],
)

http_file(
    name = "libatspi2.0-0_2.30.0-7_amd64",
    downloaded_file_path = "libatspi2.0-0_2.30.0-7_amd64.deb",
    sha256 = "8ff1ab1508799679e3209188c5f4765c3da16a7876e220c49d805ef03cced397",
    urls = ["http://deb.debian.org/debian/pool/main/a/at-spi2-core/libatspi2.0-0_2.30.0-7_amd64.deb"],
)

http_file(
    name = "libxi6_1.7.9-1_amd64",
    downloaded_file_path = "libxi6_1.7.9-1_amd64.deb",
    sha256 = "fe26733adf2025f184bf904caf088a5d3f6aa29a8863b616af9cafaad85b1237",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxi/libxi6_1.7.9-1_amd64.deb"],
)

http_file(
    name = "x11-common_7.719_all",
    downloaded_file_path = "x11-common_7.7+19_all.deb",
    sha256 = "221b2e71e0e98b8cafa4fbc674b3fbe293db031c51d35570a3c8cdfb02a5a155",
    urls = ["http://deb.debian.org/debian/pool/main/x/xorg/x11-common_7.7+19_all.deb"],
)

http_file(
    name = "libxtst6_1.2.3-1_amd64",
    downloaded_file_path = "libxtst6_1.2.3-1_amd64.deb",
    sha256 = "7072f9be17abdb9c5af7d052b19c84d1a6c1c13c30c120a98d284ba73d2da73f",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxtst/libxtst6_1.2.3-1_amd64.deb"],
)

http_file(
    name = "at-spi2-core_2.30.0-7_amd64",
    downloaded_file_path = "at-spi2-core_2.30.0-7_amd64.deb",
    sha256 = "e902f0d97a768bc0dbfd4385344e445df3a99f8558c18c55d1b6c9694cca9e42",
    urls = ["http://deb.debian.org/debian/pool/main/a/at-spi2-core/at-spi2-core_2.30.0-7_amd64.deb"],
)

http_file(
    name = "libisl19_0.20-2_amd64",
    downloaded_file_path = "libisl19_0.20-2_amd64.deb",
    sha256 = "d51e27d3fcba9bd0fe5f3303b61d08ebbd1a3bc57c40d467338b34f5d4ee762f",
    urls = ["http://deb.debian.org/debian/pool/main/i/isl/libisl19_0.20-2_amd64.deb"],
)

http_file(
    name = "libmpfr6_4.0.2-1_amd64",
    downloaded_file_path = "libmpfr6_4.0.2-1_amd64.deb",
    sha256 = "d005438229811b09ea9783491c98b145c9bcf6489284ad7870c19d2d09a8f571",
    urls = ["http://deb.debian.org/debian/pool/main/m/mpfr4/libmpfr6_4.0.2-1_amd64.deb"],
)

http_file(
    name = "libmpc3_1.1.0-1_amd64",
    downloaded_file_path = "libmpc3_1.1.0-1_amd64.deb",
    sha256 = "a73b05c10399636a7c7bff266205de05631dc4af502bfb441cbbc6af0a7deb2a",
    urls = ["http://deb.debian.org/debian/pool/main/m/mpclib3/libmpc3_1.1.0-1_amd64.deb"],
)

http_file(
    name = "cpp-8_8.3.0-6_amd64",
    downloaded_file_path = "cpp-8_8.3.0-6_amd64.deb",
    sha256 = "64f5f6fd7559a9f431132f24aa6ec9bfd062b0473f2e403c98dbd638be65f9c0",
    urls = ["http://deb.debian.org/debian/pool/main/g/gcc-8/cpp-8_8.3.0-6_amd64.deb"],
)

http_file(
    name = "cpp_8.3.0-1_amd64",
    downloaded_file_path = "cpp_8.3.0-1_amd64.deb",
    sha256 = "e94da3a26503099b1787d865127687941bd527d278b59a607bfa533f9b94d232",
    urls = ["http://deb.debian.org/debian/pool/main/g/gcc-defaults/cpp_8.3.0-1_amd64.deb"],
)

http_file(
    name = "dbus-user-session_1.12.20-0deb10u1_amd64",
    downloaded_file_path = "dbus-user-session_1.12.20-0+deb10u1_amd64.deb",
    sha256 = "009c6ea91a3cc1b23cadc94038e06f5b572dcf5ee15268304e30d9980193fc7f",
    urls = ["http://deb.debian.org/debian/pool/main/d/dbus/dbus-user-session_1.12.20-0+deb10u1_amd64.deb"],
)

http_file(
    name = "dbus-x11_1.12.20-0deb10u1_amd64",
    downloaded_file_path = "dbus-x11_1.12.20-0+deb10u1_amd64.deb",
    sha256 = "ad60963a7926bf506b26800ec0970b1ad19419ee3ac58d53897ebde5500f50b1",
    urls = ["http://deb.debian.org/debian/pool/main/d/dbus/dbus-x11_1.12.20-0+deb10u1_amd64.deb"],
)

http_file(
    name = "libdconf1_0.30.1-2_amd64",
    downloaded_file_path = "libdconf1_0.30.1-2_amd64.deb",
    sha256 = "22775563fd803db3dafe4fcc93950f72acf04e3d87b51b3dd5c107b21105a5ff",
    urls = ["http://deb.debian.org/debian/pool/main/d/dconf/libdconf1_0.30.1-2_amd64.deb"],
)

http_file(
    name = "dconf-service_0.30.1-2_amd64",
    downloaded_file_path = "dconf-service_0.30.1-2_amd64.deb",
    sha256 = "1adc68353e17f12ceb3f2e01bb0cb4e5d11b547b9436a89fa0209c46cf028c51",
    urls = ["http://deb.debian.org/debian/pool/main/d/dconf/dconf-service_0.30.1-2_amd64.deb"],
)

http_file(
    name = "dconf-gsettings-backend_0.30.1-2_amd64",
    downloaded_file_path = "dconf-gsettings-backend_0.30.1-2_amd64.deb",
    sha256 = "8dd9f676ed51db557cfdbb107542bf5406627dc1c83ded565149f02abb60e268",
    urls = ["http://deb.debian.org/debian/pool/main/d/dconf/dconf-gsettings-backend_0.30.1-2_amd64.deb"],
)

http_file(
    name = "fonts-liberation_1.07.4-9_all",
    downloaded_file_path = "fonts-liberation_1.07.4-9_all.deb",
    sha256 = "c936aebbfd0af7851399ae5ab08bb01744f5e3381f7678fb87cc77114f95ef53",
    urls = ["http://deb.debian.org/debian/pool/main/f/fonts-liberation/fonts-liberation_1.07.4-9_all.deb"],
)

http_file(
    name = "libproxy1v5_0.4.15-5deb10u1_amd64",
    downloaded_file_path = "libproxy1v5_0.4.15-5+deb10u1_amd64.deb",
    sha256 = "4dc8ecc24bcc164169f593ab1c387eacea383e3da8c46d5c0d5877fe7da1b967",
    urls = ["http://deb.debian.org/debian/pool/main/libp/libproxy/libproxy1v5_0.4.15-5+deb10u1_amd64.deb"],
)

http_file(
    name = "glib-networking-common_2.58.0-2deb10u2_all",
    downloaded_file_path = "glib-networking-common_2.58.0-2+deb10u2_all.deb",
    sha256 = "ef84896340440b25871c1e3f1d102837290db24382ad40ac74c769a70b1203be",
    urls = ["http://deb.debian.org/debian/pool/main/g/glib-networking/glib-networking-common_2.58.0-2+deb10u2_all.deb"],
)

http_file(
    name = "glib-networking-services_2.58.0-2deb10u2_amd64",
    downloaded_file_path = "glib-networking-services_2.58.0-2+deb10u2_amd64.deb",
    sha256 = "d4e2ec66aeb3e4a35d80710c69eebdb949bc4731d4b77e3beb1447988f554dbd",
    urls = ["http://deb.debian.org/debian/pool/main/g/glib-networking/glib-networking-services_2.58.0-2+deb10u2_amd64.deb"],
)

http_file(
    name = "gsettings-desktop-schemas_3.28.1-1_all",
    downloaded_file_path = "gsettings-desktop-schemas_3.28.1-1_all.deb",
    sha256 = "a75aed8781a781c4b819b2d1e952791b123580b1a02a4bb35fdbbba2e3ab8310",
    urls = ["http://deb.debian.org/debian/pool/main/g/gsettings-desktop-schemas/gsettings-desktop-schemas_3.28.1-1_all.deb"],
)

http_file(
    name = "glib-networking_2.58.0-2deb10u2_amd64",
    downloaded_file_path = "glib-networking_2.58.0-2+deb10u2_amd64.deb",
    sha256 = "6906c9748b7f5f909649b20bf97496aaddfe507c8bc146dc96310cfad3bc8fbb",
    urls = ["http://deb.debian.org/debian/pool/main/g/glib-networking/glib-networking_2.58.0-2+deb10u2_amd64.deb"],
)

http_file(
    name = "libroken18-heimdal_7.5.0dfsg-3_amd64",
    downloaded_file_path = "libroken18-heimdal_7.5.0+dfsg-3_amd64.deb",
    sha256 = "07b86db56dc66d976c16861de0bb99a80fd716ac1f9232780685f310125d19b5",
    urls = ["http://deb.debian.org/debian/pool/main/h/heimdal/libroken18-heimdal_7.5.0+dfsg-3_amd64.deb"],
)

http_file(
    name = "libasn1-8-heimdal_7.5.0dfsg-3_amd64",
    downloaded_file_path = "libasn1-8-heimdal_7.5.0+dfsg-3_amd64.deb",
    sha256 = "ab3e5b73a5f8f6cce5632a07726ffc43bddb0082fb0ac665dbf8f59f14b40995",
    urls = ["http://deb.debian.org/debian/pool/main/h/heimdal/libasn1-8-heimdal_7.5.0+dfsg-3_amd64.deb"],
)

http_file(
    name = "libasound2-data_1.1.8-1_all",
    downloaded_file_path = "libasound2-data_1.1.8-1_all.deb",
    sha256 = "c966dc292b5558a4f88b3ac992a0f5c89a3e3b579dff32c844a5bff2dbc7f7ee",
    urls = ["http://deb.debian.org/debian/pool/main/a/alsa-lib/libasound2-data_1.1.8-1_all.deb"],
)

http_file(
    name = "libasound2_1.1.8-1_amd64",
    downloaded_file_path = "libasound2_1.1.8-1_amd64.deb",
    sha256 = "6cc281b4a6d1faffe4fc6d83ec71365c1af0ee6d7806fa122fef00f85a0dde62",
    urls = ["http://deb.debian.org/debian/pool/main/a/alsa-lib/libasound2_1.1.8-1_amd64.deb"],
)

http_file(
    name = "libatk1.0-data_2.30.0-2_all",
    downloaded_file_path = "libatk1.0-data_2.30.0-2_all.deb",
    sha256 = "cf0c94611ff2245ae31d12a5a43971eb4ca628f42e93b0e003fd2c4c0de5e533",
    urls = ["http://deb.debian.org/debian/pool/main/a/atk1.0/libatk1.0-data_2.30.0-2_all.deb"],
)

http_file(
    name = "libatk1.0-0_2.30.0-2_amd64",
    downloaded_file_path = "libatk1.0-0_2.30.0-2_amd64.deb",
    sha256 = "51603cc054baa82cee4cd50ac41578266e1321ef1c74bccbb78a3dcf1729d168",
    urls = ["http://deb.debian.org/debian/pool/main/a/atk1.0/libatk1.0-0_2.30.0-2_amd64.deb"],
)

http_file(
    name = "libatk-bridge2.0-0_2.30.0-5_amd64",
    downloaded_file_path = "libatk-bridge2.0-0_2.30.0-5_amd64.deb",
    sha256 = "52ed3333fd0e1430b573343fc65d594a075ee5f493b8cbff0f64d5f41f6f3f8f",
    urls = ["http://deb.debian.org/debian/pool/main/a/at-spi2-atk/libatk-bridge2.0-0_2.30.0-5_amd64.deb"],
)

http_file(
    name = "libatm1_2.5.1-2_amd64",
    downloaded_file_path = "libatm1_2.5.1-2_amd64.deb",
    sha256 = "92e57f48dbc899730bbfed649bf1ab575052b3f93ad1ff7e3da2328c35294810",
    urls = ["http://deb.debian.org/debian/pool/main/l/linux-atm/libatm1_2.5.1-2_amd64.deb"],
)

http_file(
    name = "libauthen-sasl-perl_2.1600-1_all",
    downloaded_file_path = "libauthen-sasl-perl_2.1600-1_all.deb",
    sha256 = "164393f9244a382decdf51fbf28359ec5e07f0f6991dbb8624ec1d08fb19994d",
    urls = ["http://deb.debian.org/debian/pool/main/liba/libauthen-sasl-perl/libauthen-sasl-perl_2.1600-1_all.deb"],
)

http_file(
    name = "libavahi-common-data_0.7-4deb10u1_amd64",
    downloaded_file_path = "libavahi-common-data_0.7-4+deb10u1_amd64.deb",
    sha256 = "30fb6ade397497cc15b1ec6b66998ac4078a928602a05f602700358d5929f729",
    urls = ["http://deb.debian.org/debian/pool/main/a/avahi/libavahi-common-data_0.7-4+deb10u1_amd64.deb"],
)

http_file(
    name = "libavahi-common3_0.7-4deb10u1_amd64",
    downloaded_file_path = "libavahi-common3_0.7-4+deb10u1_amd64.deb",
    sha256 = "5a3f333cc0f56054d9003c8e28a116a77be9227110bd648dc05db1b1fb9e48a1",
    urls = ["http://deb.debian.org/debian/pool/main/a/avahi/libavahi-common3_0.7-4+deb10u1_amd64.deb"],
)

http_file(
    name = "libavahi-client3_0.7-4deb10u1_amd64",
    downloaded_file_path = "libavahi-client3_0.7-4+deb10u1_amd64.deb",
    sha256 = "fe553e88db5448b19fe3900b4923c7a77cbbb3cfe3f80f94111df65128fa35b9",
    urls = ["http://deb.debian.org/debian/pool/main/a/avahi/libavahi-client3_0.7-4+deb10u1_amd64.deb"],
)

http_file(
    name = "libbrotli1_1.0.7-2deb10u1_amd64",
    downloaded_file_path = "libbrotli1_1.0.7-2+deb10u1_amd64.deb",
    sha256 = "3797549f66e2cc3bdad58bab8eac59a1837efcb50e9325ea49c197b59fa2dae0",
    urls = ["http://deb.debian.org/debian/pool/main/b/brotli/libbrotli1_1.0.7-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libcairo-gobject2_1.16.0-4deb10u1_amd64",
    downloaded_file_path = "libcairo-gobject2_1.16.0-4+deb10u1_amd64.deb",
    sha256 = "c1e74a6852ad0b299b9197ca00ba091ef681538325d1f81680b4deb29adf094c",
    urls = ["http://deb.debian.org/debian/pool/main/c/cairo/libcairo-gobject2_1.16.0-4+deb10u1_amd64.deb"],
)

http_file(
    name = "liblcms2-2_2.9-3_amd64",
    downloaded_file_path = "liblcms2-2_2.9-3_amd64.deb",
    sha256 = "6dd806a326519b98ed9e54b184b4da2d256c4d516e75d0a38f2f6059e14eb325",
    urls = ["http://deb.debian.org/debian/pool/main/l/lcms2/liblcms2-2_2.9-3_amd64.deb"],
)

http_file(
    name = "libcolord2_1.4.3-4_amd64",
    downloaded_file_path = "libcolord2_1.4.3-4_amd64.deb",
    sha256 = "2fd78fc761cc8465702ce4ec03bc6922b172e47f524c7c64312dcf2ad0db1489",
    urls = ["http://deb.debian.org/debian/pool/main/c/colord/libcolord2_1.4.3-4_amd64.deb"],
)

http_file(
    name = "libkeyutils1_1.6-6_amd64",
    downloaded_file_path = "libkeyutils1_1.6-6_amd64.deb",
    sha256 = "0c199af9431db289ba5b34a4f21e30a4f1b6c5305203da9298096fce1cdcdb97",
    urls = ["http://deb.debian.org/debian/pool/main/k/keyutils/libkeyutils1_1.6-6_amd64.deb"],
)

http_file(
    name = "libkrb5support0_1.17-3deb10u3_amd64",
    downloaded_file_path = "libkrb5support0_1.17-3+deb10u3_amd64.deb",
    sha256 = "35fa7c3404f89deec94b8e114779efcaff68b641694f96af473f1336f5fd12f9",
    urls = ["http://deb.debian.org/debian/pool/main/k/krb5/libkrb5support0_1.17-3+deb10u3_amd64.deb"],
)

http_file(
    name = "libk5crypto3_1.17-3deb10u3_amd64",
    downloaded_file_path = "libk5crypto3_1.17-3+deb10u3_amd64.deb",
    sha256 = "58e458be3f2eb90308a4b30e27ce790f16ba20e52e9ec17c7b91ffdc7cb76cba",
    urls = ["http://deb.debian.org/debian/pool/main/k/krb5/libk5crypto3_1.17-3+deb10u3_amd64.deb"],
)

http_file(
    name = "libkrb5-3_1.17-3deb10u3_amd64",
    downloaded_file_path = "libkrb5-3_1.17-3+deb10u3_amd64.deb",
    sha256 = "b33a0fb0338e56a86d43429f4101993c91845399a085f35c3f0cf4ae5d4e8128",
    urls = ["http://deb.debian.org/debian/pool/main/k/krb5/libkrb5-3_1.17-3+deb10u3_amd64.deb"],
)

http_file(
    name = "libgssapi-krb5-2_1.17-3deb10u3_amd64",
    downloaded_file_path = "libgssapi-krb5-2_1.17-3+deb10u3_amd64.deb",
    sha256 = "77eea03f0159284459d2c3be4a0baf40cdb4495f097ae56ca99318bfbd505fc9",
    urls = ["http://deb.debian.org/debian/pool/main/k/krb5/libgssapi-krb5-2_1.17-3+deb10u3_amd64.deb"],
)

http_file(
    name = "libcups2_2.2.10-6deb10u4_amd64",
    downloaded_file_path = "libcups2_2.2.10-6+deb10u4_amd64.deb",
    sha256 = "62ea6f113f85d575fa293904cd48c4b644a616225a52c124c1e8e8e4810a20d6",
    urls = ["http://deb.debian.org/debian/pool/main/c/cups/libcups2_2.2.10-6+deb10u4_amd64.deb"],
)

http_file(
    name = "libnghttp2-14_1.36.0-2deb10u1_amd64",
    downloaded_file_path = "libnghttp2-14_1.36.0-2+deb10u1_amd64.deb",
    sha256 = "6980055df5f62aea9a32c6cc44fe231ca66cc9a251b091bd0b7e3274f4ce2a19",
    urls = ["http://deb.debian.org/debian/pool/main/n/nghttp2/libnghttp2-14_1.36.0-2+deb10u1_amd64.deb"],
)

http_file(
    name = "librtmp1_2.420151223.gitfa8646d.1-2_amd64",
    downloaded_file_path = "librtmp1_2.4+20151223.gitfa8646d.1-2_amd64.deb",
    sha256 = "506fc9e1fc66f34e6f3f79555619cc12a15388c3bdd5387c1e89d78b19d1b5dc",
    urls = ["http://deb.debian.org/debian/pool/main/r/rtmpdump/librtmp1_2.4+20151223.gitfa8646d.1-2_amd64.deb"],
)

http_file(
    name = "libssh2-1_1.8.0-2.1_amd64",
    downloaded_file_path = "libssh2-1_1.8.0-2.1_amd64.deb",
    sha256 = "0226c5853f5e48d7e99796c2e6332591383e9c337ac588e1b689f537abd0a891",
    urls = ["http://deb.debian.org/debian/pool/main/libs/libssh2/libssh2-1_1.8.0-2.1_amd64.deb"],
)

http_file(
    name = "libcurl3-gnutls_7.64.0-4deb10u2_amd64",
    downloaded_file_path = "libcurl3-gnutls_7.64.0-4+deb10u2_amd64.deb",
    sha256 = "bfe00e4eb994d2a50bc21371fe0f41228e9786bea4dfd6f52f2038f62190c801",
    urls = ["http://deb.debian.org/debian/pool/main/c/curl/libcurl3-gnutls_7.64.0-4+deb10u2_amd64.deb"],
)

http_file(
    name = "libnspr4_4.20-1_amd64",
    downloaded_file_path = "libnspr4_4.20-1_amd64.deb",
    sha256 = "e6188fdd91ec215d12d4eca5211c2406874eb17f5b1c09d6355641a349adcec0",
    urls = ["http://deb.debian.org/debian/pool/main/n/nspr/libnspr4_4.20-1_amd64.deb"],
)

http_file(
    name = "libnss3_3.42.1-1deb10u3_amd64",
    downloaded_file_path = "libnss3_3.42.1-1+deb10u3_amd64.deb",
    sha256 = "7c02ebbba5b27e98bef9843cf6117d1621c9944aefd2a35e1a09feb021180b4e",
    urls = ["http://deb.debian.org/debian/pool/main/n/nss/libnss3_3.42.1-1+deb10u3_amd64.deb"],
)

http_file(
    name = "libcurl3-nss_7.64.0-4deb10u2_amd64",
    downloaded_file_path = "libcurl3-nss_7.64.0-4+deb10u2_amd64.deb",
    sha256 = "2364f0f3e7cea621fef486c54dee02de53b50e5d9b2af88b3699fdaf6d9ec546",
    urls = ["http://deb.debian.org/debian/pool/main/c/curl/libcurl3-nss_7.64.0-4+deb10u2_amd64.deb"],
)

http_file(
    name = "libcurl4_7.64.0-4deb10u2_amd64",
    downloaded_file_path = "libcurl4_7.64.0-4+deb10u2_amd64.deb",
    sha256 = "9a0585aa42353bb7b4cd0b863f00ec2faf68b4a9d5cad2d3720317c059a50e90",
    urls = ["http://deb.debian.org/debian/pool/main/c/curl/libcurl4_7.64.0-4+deb10u2_amd64.deb"],
)

http_file(
    name = "libdata-dump-perl_1.23-1_all",
    downloaded_file_path = "libdata-dump-perl_1.23-1_all.deb",
    sha256 = "0d15d8c02d2d0cb390e61b831ef336841764724d6adad326319af042ceacd71f",
    urls = ["http://deb.debian.org/debian/pool/main/libd/libdata-dump-perl/libdata-dump-perl_1.23-1_all.deb"],
)

http_file(
    name = "libdrm-common_2.4.97-1_all",
    downloaded_file_path = "libdrm-common_2.4.97-1_all.deb",
    sha256 = "eea378d3dab56923e06871331838aecc38a35aad997da7fc96a5e8c4e36081a2",
    urls = ["http://deb.debian.org/debian/pool/main/libd/libdrm/libdrm-common_2.4.97-1_all.deb"],
)

http_file(
    name = "libdrm2_2.4.97-1_amd64",
    downloaded_file_path = "libdrm2_2.4.97-1_amd64.deb",
    sha256 = "759caef1fbf885c515ae7273cdf969d185cf7276b432a813c46651e468c57489",
    urls = ["http://deb.debian.org/debian/pool/main/libd/libdrm/libdrm2_2.4.97-1_amd64.deb"],
)

http_file(
    name = "libdrm-amdgpu1_2.4.97-1_amd64",
    downloaded_file_path = "libdrm-amdgpu1_2.4.97-1_amd64.deb",
    sha256 = "283bff4909f50da051f057cf6b8e84c590675ede91e57ce7414d2f1d4097b691",
    urls = ["http://deb.debian.org/debian/pool/main/libd/libdrm/libdrm-amdgpu1_2.4.97-1_amd64.deb"],
)

http_file(
    name = "libpciaccess0_0.14-1_amd64",
    downloaded_file_path = "libpciaccess0_0.14-1_amd64.deb",
    sha256 = "5f6cc48ee748200858ab56f43a47534731f5012c2c7c936a364b5c52c0cbe809",
    urls = ["http://deb.debian.org/debian/pool/main/libp/libpciaccess/libpciaccess0_0.14-1_amd64.deb"],
)

http_file(
    name = "libdrm-intel1_2.4.97-1_amd64",
    downloaded_file_path = "libdrm-intel1_2.4.97-1_amd64.deb",
    sha256 = "d5cb66f82681192ae14157370c98fc12bac0331283a8afd6b2c9c1a70c910a57",
    urls = ["http://deb.debian.org/debian/pool/main/libd/libdrm/libdrm-intel1_2.4.97-1_amd64.deb"],
)

http_file(
    name = "libdrm-nouveau2_2.4.97-1_amd64",
    downloaded_file_path = "libdrm-nouveau2_2.4.97-1_amd64.deb",
    sha256 = "875b604283ad5b56fb0ae0ec28b4e52ba3055ce9116e71d4bcec7854b67ba7b6",
    urls = ["http://deb.debian.org/debian/pool/main/libd/libdrm/libdrm-nouveau2_2.4.97-1_amd64.deb"],
)

http_file(
    name = "libdrm-radeon1_2.4.97-1_amd64",
    downloaded_file_path = "libdrm-radeon1_2.4.97-1_amd64.deb",
    sha256 = "e7e98f7beedfb326a3dc4d2cef3eff144c7cfe22bef99c2004708c1aa5cceb8c",
    urls = ["http://deb.debian.org/debian/pool/main/libd/libdrm/libdrm-radeon1_2.4.97-1_amd64.deb"],
)

http_file(
    name = "libedit2_3.1-20181209-1_amd64",
    downloaded_file_path = "libedit2_3.1-20181209-1_amd64.deb",
    sha256 = "ccd6cdf5ec28a92744a79f3f210f071679d12deb36917d4e8d17ae7587f218cc",
    urls = ["http://deb.debian.org/debian/pool/main/libe/libedit/libedit2_3.1-20181209-1_amd64.deb"],
)

http_file(
    name = "libencode-locale-perl_1.05-1_all",
    downloaded_file_path = "libencode-locale-perl_1.05-1_all.deb",
    sha256 = "e96846869eeadd1de4e6ea594da14191cfc7838f1fa48ab880978d764d3c7ee6",
    urls = ["http://deb.debian.org/debian/pool/main/libe/libencode-locale-perl/libencode-locale-perl_1.05-1_all.deb"],
)

http_file(
    name = "libepoxy0_1.5.3-0.1_amd64",
    downloaded_file_path = "libepoxy0_1.5.3-0.1_amd64.deb",
    sha256 = "968295ae7382be0fc06e535f2a1408f54b0b29096e0142618d185da1c7a42ed0",
    urls = ["http://deb.debian.org/debian/pool/main/libe/libepoxy/libepoxy0_1.5.3-0.1_amd64.deb"],
)

http_file(
    name = "libevent-core-2.1-6_2.1.8-stable-4_amd64",
    downloaded_file_path = "libevent-core-2.1-6_2.1.8-stable-4_amd64.deb",
    sha256 = "a96168d513725033c6558c49b191ae192a0eb3b92dd574f540b163ce19549323",
    urls = ["http://deb.debian.org/debian/pool/main/libe/libevent/libevent-core-2.1-6_2.1.8-stable-4_amd64.deb"],
)

http_file(
    name = "libevent-pthreads-2.1-6_2.1.8-stable-4_amd64",
    downloaded_file_path = "libevent-pthreads-2.1-6_2.1.8-stable-4_amd64.deb",
    sha256 = "d2012b6f09029fd2c9a8d7a423fc7afb2fcc86c1b4b1dd46659b7e08f20e5a68",
    urls = ["http://deb.debian.org/debian/pool/main/libe/libevent/libevent-pthreads-2.1-6_2.1.8-stable-4_amd64.deb"],
)

http_file(
    name = "libipc-system-simple-perl_1.25-4_all",
    downloaded_file_path = "libipc-system-simple-perl_1.25-4_all.deb",
    sha256 = "caf0a63cd26d7fe0436d90f5e90a78d0e4719d4e39898dfa8e0ab56234dc29f3",
    urls = ["http://deb.debian.org/debian/pool/main/libi/libipc-system-simple-perl/libipc-system-simple-perl_1.25-4_all.deb"],
)

http_file(
    name = "libfile-basedir-perl_0.08-1_all",
    downloaded_file_path = "libfile-basedir-perl_0.08-1_all.deb",
    sha256 = "82549a48fe90cb583608338ebbc1e2b3410c2f4dd43df095706f6202c6ea1307",
    urls = ["http://deb.debian.org/debian/pool/main/libf/libfile-basedir-perl/libfile-basedir-perl_0.08-1_all.deb"],
)

http_file(
    name = "liburi-perl_1.76-1_all",
    downloaded_file_path = "liburi-perl_1.76-1_all.deb",
    sha256 = "1b77f6395d2869047f1cb6e15fb6e154736b61c51454949af3e857e4e30fbfd4",
    urls = ["http://deb.debian.org/debian/pool/main/libu/liburi-perl/liburi-perl_1.76-1_all.deb"],
)

http_file(
    name = "libfile-desktopentry-perl_0.22-1_all",
    downloaded_file_path = "libfile-desktopentry-perl_0.22-1_all.deb",
    sha256 = "1c485aae4b263becd5fede44705b0e92616e5300eba93387b52549be9e027a4d",
    urls = ["http://deb.debian.org/debian/pool/main/libf/libfile-desktopentry-perl/libfile-desktopentry-perl_0.22-1_all.deb"],
)

http_file(
    name = "libtimedate-perl_2.3000-2deb10u1_all",
    downloaded_file_path = "libtimedate-perl_2.3000-2+deb10u1_all.deb",
    sha256 = "a8ac73f3c579a7c8b16fd029792c196eb1ab446a49584c2bd94b2bbe30e3df3a",
    urls = ["http://deb.debian.org/debian/pool/main/libt/libtimedate-perl/libtimedate-perl_2.3000-2+deb10u1_all.deb"],
)

http_file(
    name = "libhttp-date-perl_6.02-1_all",
    downloaded_file_path = "libhttp-date-perl_6.02-1_all.deb",
    sha256 = "1caf56785e4b679aff4f7dfbd334968c27c226b6a8e39fc854a9ae35d40b6990",
    urls = ["http://deb.debian.org/debian/pool/main/libh/libhttp-date-perl/libhttp-date-perl_6.02-1_all.deb"],
)

http_file(
    name = "libfile-listing-perl_6.04-1_all",
    downloaded_file_path = "libfile-listing-perl_6.04-1_all.deb",
    sha256 = "910468093aa2ab3c29376fa7356e5802998342953991f1ba784019f2e742d5fe",
    urls = ["http://deb.debian.org/debian/pool/main/libf/libfile-listing-perl/libfile-listing-perl_6.04-1_all.deb"],
)

http_file(
    name = "libfile-mimeinfo-perl_0.29-1_all",
    downloaded_file_path = "libfile-mimeinfo-perl_0.29-1_all.deb",
    sha256 = "491e9d5a4271b55f1d9ab3e27ab67602e4f12ecad5d9e1d5861d24ef964ee7d3",
    urls = ["http://deb.debian.org/debian/pool/main/libf/libfile-mimeinfo-perl/libfile-mimeinfo-perl_0.29-1_all.deb"],
)

http_file(
    name = "libfont-afm-perl_1.20-2_all",
    downloaded_file_path = "libfont-afm-perl_1.20-2_all.deb",
    sha256 = "f54c0c6c91b7494bf5e199596334335eaf98a6a8284f96d29afe9da3636303a6",
    urls = ["http://deb.debian.org/debian/pool/main/libf/libfont-afm-perl/libfont-afm-perl_1.20-2_all.deb"],
)

http_file(
    name = "libfontenc1_1.1.3-1b2_amd64",
    downloaded_file_path = "libfontenc1_1.1.3-1+b2_amd64.deb",
    sha256 = "f456b6168c57c41bfd0bd04d7dfb445c283372585e87333e8de42eebdc92366e",
    urls = ["http://deb.debian.org/debian/pool/main/libf/libfontenc/libfontenc1_1.1.3-1+b2_amd64.deb"],
)

http_file(
    name = "libwayland-server0_1.16.0-1_amd64",
    downloaded_file_path = "libwayland-server0_1.16.0-1_amd64.deb",
    sha256 = "93c7bb9dfd107f1e7d2b3e0a9af6efc653d75cc5e58f653cfd14afc12d125655",
    urls = ["http://deb.debian.org/debian/pool/main/w/wayland/libwayland-server0_1.16.0-1_amd64.deb"],
)

http_file(
    name = "libgbm1_18.3.6-2deb10u1_amd64",
    downloaded_file_path = "libgbm1_18.3.6-2+deb10u1_amd64.deb",
    sha256 = "74c468c77990d871243ae31c108b7b770f5a0ff02fd36316f70affe52bce1999",
    urls = ["http://deb.debian.org/debian/pool/main/m/mesa/libgbm1_18.3.6-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libgdk-pixbuf2.0-bin_2.38.1dfsg-1_amd64",
    downloaded_file_path = "libgdk-pixbuf2.0-bin_2.38.1+dfsg-1_amd64.deb",
    sha256 = "1568ec195925390b0dee957706fa5c46251411b2ee0d0a60add13ae9bc571240",
    urls = ["http://deb.debian.org/debian/pool/main/g/gdk-pixbuf/libgdk-pixbuf2.0-bin_2.38.1+dfsg-1_amd64.deb"],
)

http_file(
    name = "libglapi-mesa_18.3.6-2deb10u1_amd64",
    downloaded_file_path = "libglapi-mesa_18.3.6-2+deb10u1_amd64.deb",
    sha256 = "400fa15a8da369359328ad41ac893c4cb51686514ee6a9456dbbfd12e8836ec3",
    urls = ["http://deb.debian.org/debian/pool/main/m/mesa/libglapi-mesa_18.3.6-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libllvm7_7.0.1-8deb10u2_amd64",
    downloaded_file_path = "libllvm7_7.0.1-8+deb10u2_amd64.deb",
    sha256 = "1918442d7db16b0fadb38f2ab7f15d3bb06688923ad5929fb78e706a015b0192",
    urls = ["http://deb.debian.org/debian/pool/main/l/llvm-toolchain-7/libllvm7_7.0.1-8+deb10u2_amd64.deb"],
)

http_file(
    name = "libsensors-config_3.5.0-3_all",
    downloaded_file_path = "libsensors-config_3.5.0-3_all.deb",
    sha256 = "a064dbafa1590562e979852aca9802fc10ecfb6fda5403369c903fb38fa9802a",
    urls = ["http://deb.debian.org/debian/pool/main/l/lm-sensors/libsensors-config_3.5.0-3_all.deb"],
)

http_file(
    name = "libsensors5_3.5.0-3_amd64",
    downloaded_file_path = "libsensors5_3.5.0-3_amd64.deb",
    sha256 = "363ea208bfe6bf3dd1f66914eae5a15373fef0d72f84df013eb6d60633866c50",
    urls = ["http://deb.debian.org/debian/pool/main/l/lm-sensors/libsensors5_3.5.0-3_amd64.deb"],
)

http_file(
    name = "libgl1-mesa-dri_18.3.6-2deb10u1_amd64",
    downloaded_file_path = "libgl1-mesa-dri_18.3.6-2+deb10u1_amd64.deb",
    sha256 = "964968e2914e86eca243c9a316529a4d2f8b6e000f981e9a0891ac3c3550be32",
    urls = ["http://deb.debian.org/debian/pool/main/m/mesa/libgl1-mesa-dri_18.3.6-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libglib2.0-data_2.58.3-2deb10u3_all",
    downloaded_file_path = "libglib2.0-data_2.58.3-2+deb10u3_all.deb",
    sha256 = "bc04bacc04c1cec8908d61ccbd801cb079541aeb28443fc961c177c62fa5ae88",
    urls = ["http://deb.debian.org/debian/pool/main/g/glib2.0/libglib2.0-data_2.58.3-2+deb10u3_all.deb"],
)

http_file(
    name = "libglvnd0_1.1.0-1_amd64",
    downloaded_file_path = "libglvnd0_1.1.0-1_amd64.deb",
    sha256 = "4247b31689649f12d7429f337d038ce73cb8394d7a3a25eac466536a008f00c6",
    urls = ["http://deb.debian.org/debian/pool/main/libg/libglvnd/libglvnd0_1.1.0-1_amd64.deb"],
)

http_file(
    name = "libx11-xcb1_1.6.7-1deb10u2_amd64",
    downloaded_file_path = "libx11-xcb1_1.6.7-1+deb10u2_amd64.deb",
    sha256 = "86bfbd42b38ef7ad73ca8a10cd6445dc38457e361c35ac57a2dda3a74147e7ff",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libx11/libx11-xcb1_1.6.7-1+deb10u2_amd64.deb"],
)

http_file(
    name = "libxcb-dri2-0_1.13.1-2_amd64",
    downloaded_file_path = "libxcb-dri2-0_1.13.1-2_amd64.deb",
    sha256 = "1604da91e88a88395add6588d8b6227098acc2680ee1f234697219036f4d22b1",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxcb/libxcb-dri2-0_1.13.1-2_amd64.deb"],
)

http_file(
    name = "libxcb-dri3-0_1.13.1-2_amd64",
    downloaded_file_path = "libxcb-dri3-0_1.13.1-2_amd64.deb",
    sha256 = "931d9c7be021a45ae69fb99f72fde393402f3d38355ecbcf8c1742e19749a0df",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxcb/libxcb-dri3-0_1.13.1-2_amd64.deb"],
)

http_file(
    name = "libxcb-glx0_1.13.1-2_amd64",
    downloaded_file_path = "libxcb-glx0_1.13.1-2_amd64.deb",
    sha256 = "ba58285fe011506fed6e2401e5623d924542864362eb68d5e724555af5195d11",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxcb/libxcb-glx0_1.13.1-2_amd64.deb"],
)

http_file(
    name = "libxcb-present0_1.13.1-2_amd64",
    downloaded_file_path = "libxcb-present0_1.13.1-2_amd64.deb",
    sha256 = "fb531c51237c2371bc9a9924f3e70b15fb004181444473bc932b7ad9263500cb",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxcb/libxcb-present0_1.13.1-2_amd64.deb"],
)

http_file(
    name = "libxcb-sync1_1.13.1-2_amd64",
    downloaded_file_path = "libxcb-sync1_1.13.1-2_amd64.deb",
    sha256 = "991807437dc07687ae2622f0e6ee8aff87695e13003921f469e5b6a495f55e3b",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxcb/libxcb-sync1_1.13.1-2_amd64.deb"],
)

http_file(
    name = "libxfixes3_5.0.3-1_amd64",
    downloaded_file_path = "libxfixes3_5.0.3-1_amd64.deb",
    sha256 = "3b307490c669accd52dc627ad4dc269a03632ca512fbc7b185b572f76608ff4e",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxfixes/libxfixes3_5.0.3-1_amd64.deb"],
)

http_file(
    name = "libxdamage1_1.1.4-3b3_amd64",
    downloaded_file_path = "libxdamage1_1.1.4-3+b3_amd64.deb",
    sha256 = "e9539838d47cb10b4273c320f8e885ef85df7bd3a95f0ea9bcbc144db82c03ae",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxdamage/libxdamage1_1.1.4-3+b3_amd64.deb"],
)

http_file(
    name = "libxshmfence1_1.3-1_amd64",
    downloaded_file_path = "libxshmfence1_1.3-1_amd64.deb",
    sha256 = "1a38142e40e3d32dc4f9a326bf5617363b7d9b4bb762fdcdd262f2192092024d",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxshmfence/libxshmfence1_1.3-1_amd64.deb"],
)

http_file(
    name = "libxxf86vm1_1.1.4-1b2_amd64",
    downloaded_file_path = "libxxf86vm1_1.1.4-1+b2_amd64.deb",
    sha256 = "6f4ca916aaec26d7000fa7f58de3f71119309ab7590ce1f517abfe1825a676c7",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxxf86vm/libxxf86vm1_1.1.4-1+b2_amd64.deb"],
)

http_file(
    name = "libglx-mesa0_18.3.6-2deb10u1_amd64",
    downloaded_file_path = "libglx-mesa0_18.3.6-2+deb10u1_amd64.deb",
    sha256 = "0d25475d75cf870387a70afb2809aa79c33c7d05fe333bc9b2e1c4a258489ce7",
    urls = ["http://deb.debian.org/debian/pool/main/m/mesa/libglx-mesa0_18.3.6-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libheimbase1-heimdal_7.5.0dfsg-3_amd64",
    downloaded_file_path = "libheimbase1-heimdal_7.5.0+dfsg-3_amd64.deb",
    sha256 = "00bdfafe36ef55f48b08acd5b838efe17e84a042dd9dee8cf314f5babe3dce02",
    urls = ["http://deb.debian.org/debian/pool/main/h/heimdal/libheimbase1-heimdal_7.5.0+dfsg-3_amd64.deb"],
)

http_file(
    name = "libhcrypto4-heimdal_7.5.0dfsg-3_amd64",
    downloaded_file_path = "libhcrypto4-heimdal_7.5.0+dfsg-3_amd64.deb",
    sha256 = "26716616b685b8a3e3ec71d842017e9d190cb77a802756936a2e2d193db2d198",
    urls = ["http://deb.debian.org/debian/pool/main/h/heimdal/libhcrypto4-heimdal_7.5.0+dfsg-3_amd64.deb"],
)

http_file(
    name = "libwind0-heimdal_7.5.0dfsg-3_amd64",
    downloaded_file_path = "libwind0-heimdal_7.5.0+dfsg-3_amd64.deb",
    sha256 = "5876a595cfd993788bd9df1e745dc92652898ab81fae8c2c76bc3d89951f534b",
    urls = ["http://deb.debian.org/debian/pool/main/h/heimdal/libwind0-heimdal_7.5.0+dfsg-3_amd64.deb"],
)

http_file(
    name = "libhx509-5-heimdal_7.5.0dfsg-3_amd64",
    downloaded_file_path = "libhx509-5-heimdal_7.5.0+dfsg-3_amd64.deb",
    sha256 = "e467fad27ed3682c612ac43df2723775183be2b5004669d82676a9ef5fb3dbeb",
    urls = ["http://deb.debian.org/debian/pool/main/h/heimdal/libhx509-5-heimdal_7.5.0+dfsg-3_amd64.deb"],
)

http_file(
    name = "libkrb5-26-heimdal_7.5.0dfsg-3_amd64",
    downloaded_file_path = "libkrb5-26-heimdal_7.5.0+dfsg-3_amd64.deb",
    sha256 = "231b4df386c79214b34302f61517ea4fe4499fee726e502764a40b7ce55d24f7",
    urls = ["http://deb.debian.org/debian/pool/main/h/heimdal/libkrb5-26-heimdal_7.5.0+dfsg-3_amd64.deb"],
)

http_file(
    name = "libheimntlm0-heimdal_7.5.0dfsg-3_amd64",
    downloaded_file_path = "libheimntlm0-heimdal_7.5.0+dfsg-3_amd64.deb",
    sha256 = "17d23850e1d5d6e2628a5cb7a922cf13d6889b39bf0836f7731b32e9cdf61d4b",
    urls = ["http://deb.debian.org/debian/pool/main/h/heimdal/libheimntlm0-heimdal_7.5.0+dfsg-3_amd64.deb"],
)

http_file(
    name = "libgssapi3-heimdal_7.5.0dfsg-3_amd64",
    downloaded_file_path = "libgssapi3-heimdal_7.5.0+dfsg-3_amd64.deb",
    sha256 = "a9a4a4cf9587d8ead87667adc7175b1a54023f47a7c448277bcc2eed2e2d5fe0",
    urls = ["http://deb.debian.org/debian/pool/main/h/heimdal/libgssapi3-heimdal_7.5.0+dfsg-3_amd64.deb"],
)

http_file(
    name = "libjson-glib-1.0-common_1.4.4-2_all",
    downloaded_file_path = "libjson-glib-1.0-common_1.4.4-2_all.deb",
    sha256 = "c27dbb0cf9c73e2a09d5c774fb46ecf6d2b634facaf3b37b20a4654d9c549187",
    urls = ["http://deb.debian.org/debian/pool/main/j/json-glib/libjson-glib-1.0-common_1.4.4-2_all.deb"],
)

http_file(
    name = "libjson-glib-1.0-0_1.4.4-2_amd64",
    downloaded_file_path = "libjson-glib-1.0-0_1.4.4-2_amd64.deb",
    sha256 = "58f872df6bc521a7ef4990c2a4b3264b1a1fab15440297a7e92ef88067e308ed",
    urls = ["http://deb.debian.org/debian/pool/main/j/json-glib/libjson-glib-1.0-0_1.4.4-2_amd64.deb"],
)

http_file(
    name = "libsoup2.4-1_2.64.2-2_amd64",
    downloaded_file_path = "libsoup2.4-1_2.64.2-2_amd64.deb",
    sha256 = "db9918e3937eb4f92068665a9b42ea33b0860da602fa5c2f0e80e5cb15a556c4",
    urls = ["http://deb.debian.org/debian/pool/main/libs/libsoup2.4/libsoup2.4-1_2.64.2-2_amd64.deb"],
)

http_file(
    name = "libsoup-gnome2.4-1_2.64.2-2_amd64",
    downloaded_file_path = "libsoup-gnome2.4-1_2.64.2-2_amd64.deb",
    sha256 = "33c571659e0fe2ba55214d2c68b15d883215c6c0e08e6037173da92585f9a623",
    urls = ["http://deb.debian.org/debian/pool/main/libs/libsoup2.4/libsoup-gnome2.4-1_2.64.2-2_amd64.deb"],
)

http_file(
    name = "librest-0.7-0_0.8.1-1_amd64",
    downloaded_file_path = "librest-0.7-0_0.8.1-1_amd64.deb",
    sha256 = "17d25479dd8fb0bfc7fd92ca92d7c063e9d0a22f43cb90e2de243b89111cde93",
    urls = ["http://deb.debian.org/debian/pool/main/libr/librest/librest-0.7-0_0.8.1-1_amd64.deb"],
)

http_file(
    name = "libwayland-client0_1.16.0-1_amd64",
    downloaded_file_path = "libwayland-client0_1.16.0-1_amd64.deb",
    sha256 = "826fdd1a6a5ffa01415f138e238da858aae22ac4f4835cedfecab76dd0dcb01b",
    urls = ["http://deb.debian.org/debian/pool/main/w/wayland/libwayland-client0_1.16.0-1_amd64.deb"],
)

http_file(
    name = "libwayland-cursor0_1.16.0-1_amd64",
    downloaded_file_path = "libwayland-cursor0_1.16.0-1_amd64.deb",
    sha256 = "eee990ea0ad68ac409986ebf92106b8deada54cc2cfd19293177f5f938c35690",
    urls = ["http://deb.debian.org/debian/pool/main/w/wayland/libwayland-cursor0_1.16.0-1_amd64.deb"],
)

http_file(
    name = "libwayland-egl1_1.16.0-1_amd64",
    downloaded_file_path = "libwayland-egl1_1.16.0-1_amd64.deb",
    sha256 = "a021e9aa9a92270fa259211a0ca69b5e8428f32c6e800a4a93f0766b0a48a5c6",
    urls = ["http://deb.debian.org/debian/pool/main/w/wayland/libwayland-egl1_1.16.0-1_amd64.deb"],
)

http_file(
    name = "libxcomposite1_0.4.4-2_amd64",
    downloaded_file_path = "libxcomposite1_0.4.4-2_amd64.deb",
    sha256 = "043c878356954f4521c401b160d554809115c472ca384d9f793c1c7542316eb9",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxcomposite/libxcomposite1_0.4.4-2_amd64.deb"],
)

http_file(
    name = "libxcursor1_1.1.15-2_amd64",
    downloaded_file_path = "libxcursor1_1.1.15-2_amd64.deb",
    sha256 = "5c5c3c5020b3e963afcf45af21ad8c0c14375ae35f6c649a05a22790503bf24c",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxcursor/libxcursor1_1.1.15-2_amd64.deb"],
)

http_file(
    name = "libxinerama1_1.1.4-2_amd64",
    downloaded_file_path = "libxinerama1_1.1.4-2_amd64.deb",
    sha256 = "f692c854935571ee44fe313541d8a9f678a4f11dc513bc43b9d0a501c6dff0bd",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxinerama/libxinerama1_1.1.4-2_amd64.deb"],
)

http_file(
    name = "xkb-data_2.26-2_all",
    downloaded_file_path = "xkb-data_2.26-2_all.deb",
    sha256 = "17d21564c940dd8d89e0a1b69d6fea0144d057e4698902378f5c83500612b779",
    urls = ["http://deb.debian.org/debian/pool/main/x/xkeyboard-config/xkb-data_2.26-2_all.deb"],
)

http_file(
    name = "libxkbcommon0_0.8.2-1_amd64",
    downloaded_file_path = "libxkbcommon0_0.8.2-1_amd64.deb",
    sha256 = "a93729f1d325598ad9c6a7ffe00c464fbe276181a3a124855041c1e303175f0c",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxkbcommon/libxkbcommon0_0.8.2-1_amd64.deb"],
)

http_file(
    name = "libxrandr2_1.5.1-1_amd64",
    downloaded_file_path = "libxrandr2_1.5.1-1_amd64.deb",
    sha256 = "8fdd8ba4a8ad819731d6bbd903b52851a2ec2f9ef4139d880e9be421ea61338c",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxrandr/libxrandr2_1.5.1-1_amd64.deb"],
)

http_file(
    name = "libgtk-3-common_3.24.5-1_all",
    downloaded_file_path = "libgtk-3-common_3.24.5-1_all.deb",
    sha256 = "1e1c979ec882542ce09b40c0f7246a7f348b42d9bec6f31eb2614a8ddccd4874",
    urls = ["http://deb.debian.org/debian/pool/main/g/gtk+3.0/libgtk-3-common_3.24.5-1_all.deb"],
)

http_file(
    name = "libgtk-3-0_3.24.5-1_amd64",
    downloaded_file_path = "libgtk-3-0_3.24.5-1_amd64.deb",
    sha256 = "e652e04b04cc8a67c24c5773180a7fdd65a6cfc55a2777722e80825a56a33729",
    urls = ["http://deb.debian.org/debian/pool/main/g/gtk+3.0/libgtk-3-0_3.24.5-1_amd64.deb"],
)

http_file(
    name = "libgtk-3-bin_3.24.5-1_amd64",
    downloaded_file_path = "libgtk-3-bin_3.24.5-1_amd64.deb",
    sha256 = "6a3c4ca2de81b9fcc33f11b76afd3a3a29abc379fb30296a4b45283a031f50c0",
    urls = ["http://deb.debian.org/debian/pool/main/g/gtk+3.0/libgtk-3-bin_3.24.5-1_amd64.deb"],
)

http_file(
    name = "libhtml-tagset-perl_3.20-3_all",
    downloaded_file_path = "libhtml-tagset-perl_3.20-3_all.deb",
    sha256 = "ab078c41e6720aaacb09d54fedaab3526c4bbba62e12f661add70eeaff762bf8",
    urls = ["http://deb.debian.org/debian/pool/main/libh/libhtml-tagset-perl/libhtml-tagset-perl_3.20-3_all.deb"],
)

http_file(
    name = "libhtml-parser-perl_3.72-3b3_amd64",
    downloaded_file_path = "libhtml-parser-perl_3.72-3+b3_amd64.deb",
    sha256 = "d1e770fbb959033ab9b9056398d59cee5dc52fa2e98e9d0776d42c8605e42393",
    urls = ["http://deb.debian.org/debian/pool/main/libh/libhtml-parser-perl/libhtml-parser-perl_3.72-3+b3_amd64.deb"],
)

http_file(
    name = "libio-html-perl_1.001-1_all",
    downloaded_file_path = "libio-html-perl_1.001-1_all.deb",
    sha256 = "20915dbac4d93416960bce90fd32d5263a04a12ed15a095694a34dca8e2a8920",
    urls = ["http://deb.debian.org/debian/pool/main/libi/libio-html-perl/libio-html-perl_1.001-1_all.deb"],
)

http_file(
    name = "liblwp-mediatypes-perl_6.02-1_all",
    downloaded_file_path = "liblwp-mediatypes-perl_6.02-1_all.deb",
    sha256 = "15b7aa4cdd8b0245736288661a5e497bb19ff7fe34dbc371f9df412ce1398372",
    urls = ["http://deb.debian.org/debian/pool/main/libl/liblwp-mediatypes-perl/liblwp-mediatypes-perl_6.02-1_all.deb"],
)

http_file(
    name = "libhttp-message-perl_6.18-1_all",
    downloaded_file_path = "libhttp-message-perl_6.18-1_all.deb",
    sha256 = "41d7ffc45347c5841ce8ad2a6511d8c1da2f3782aa1b9f1f83549d37d50991b2",
    urls = ["http://deb.debian.org/debian/pool/main/libh/libhttp-message-perl/libhttp-message-perl_6.18-1_all.deb"],
)

http_file(
    name = "libhtml-form-perl_6.03-1_all",
    downloaded_file_path = "libhtml-form-perl_6.03-1_all.deb",
    sha256 = "775ef319ec0b2dac14ba8792177fd06c5b2affb768a03fbd9a404cfbcf5255a5",
    urls = ["http://deb.debian.org/debian/pool/main/libh/libhtml-form-perl/libhtml-form-perl_6.03-1_all.deb"],
)

http_file(
    name = "libhtml-tree-perl_5.07-2_all",
    downloaded_file_path = "libhtml-tree-perl_5.07-2_all.deb",
    sha256 = "2f04bfd5b6d594f211ed43ab92ecba445c0eb4dbce56f68b92e77d0310230bba",
    urls = ["http://deb.debian.org/debian/pool/main/libh/libhtml-tree-perl/libhtml-tree-perl_5.07-2_all.deb"],
)

http_file(
    name = "libhtml-format-perl_2.12-1_all",
    downloaded_file_path = "libhtml-format-perl_2.12-1_all.deb",
    sha256 = "4793ddb1861cfc95fa29a17b3cb2de8644f56474a3b66e8db5a085af33ad8ef9",
    urls = ["http://deb.debian.org/debian/pool/main/libh/libhtml-format-perl/libhtml-format-perl_2.12-1_all.deb"],
)

http_file(
    name = "libhttp-cookies-perl_6.04-1_all",
    downloaded_file_path = "libhttp-cookies-perl_6.04-1_all.deb",
    sha256 = "db1cd5fed117936461f3d16eeb12e84e5eb2e9b6756beda02d7043142ff35396",
    urls = ["http://deb.debian.org/debian/pool/main/libh/libhttp-cookies-perl/libhttp-cookies-perl_6.04-1_all.deb"],
)

http_file(
    name = "libhttp-daemon-perl_6.01-3_all",
    downloaded_file_path = "libhttp-daemon-perl_6.01-3_all.deb",
    sha256 = "c9b91b2cd288563998d9944a3b047b3ff8f61ba61e73ab11336dd23c2cc27303",
    urls = ["http://deb.debian.org/debian/pool/main/libh/libhttp-daemon-perl/libhttp-daemon-perl_6.01-3_all.deb"],
)

http_file(
    name = "libhttp-negotiate-perl_6.01-1_all",
    downloaded_file_path = "libhttp-negotiate-perl_6.01-1_all.deb",
    sha256 = "943113a37761708f501099d56b3298765fc691bc88d2f7544d3dd78e78981adb",
    urls = ["http://deb.debian.org/debian/pool/main/libh/libhttp-negotiate-perl/libhttp-negotiate-perl_6.01-1_all.deb"],
)

http_file(
    name = "libice6_1.0.9-2_amd64",
    downloaded_file_path = "libice6_1.0.9-2_amd64.deb",
    sha256 = "5ab658c7efc05094b69f6d0950486a70df617305fab10983b7d885ab0a750f21",
    urls = ["http://deb.debian.org/debian/pool/main/libi/libice/libice6_1.0.9-2_amd64.deb"],
)

http_file(
    name = "perl-openssl-defaults_3_amd64",
    downloaded_file_path = "perl-openssl-defaults_3_amd64.deb",
    sha256 = "309f9c1ded134f9d4626ac996e893977bc1185c914e2400fa70a688340dec56e",
    urls = ["http://deb.debian.org/debian/pool/main/p/perl-openssl-defaults/perl-openssl-defaults_3_amd64.deb"],
)

http_file(
    name = "libnet-ssleay-perl_1.85-2b1_amd64",
    downloaded_file_path = "libnet-ssleay-perl_1.85-2+b1_amd64.deb",
    sha256 = "1fa585d507f5025f3601cd8d32ba8f78093b1c1eb63537784c832b7f8b485553",
    urls = ["http://deb.debian.org/debian/pool/main/libn/libnet-ssleay-perl/libnet-ssleay-perl_1.85-2+b1_amd64.deb"],
)

http_file(
    name = "libio-socket-ssl-perl_2.060-3_all",
    downloaded_file_path = "libio-socket-ssl-perl_2.060-3_all.deb",
    sha256 = "1129a248bb4585c1fb17cd40bbf87e587117e708bffd633e749fc0840daed72e",
    urls = ["http://deb.debian.org/debian/pool/main/libi/libio-socket-ssl-perl/libio-socket-ssl-perl_2.060-3_all.deb"],
)

http_file(
    name = "libio-stringy-perl_2.111-3_all",
    downloaded_file_path = "libio-stringy-perl_2.111-3_all.deb",
    sha256 = "8ecb204e260fdd9d25d7f21327cb66e5f46cd162fedd6fe2d631ad0f66699c10",
    urls = ["http://deb.debian.org/debian/pool/main/i/io-stringy/libio-stringy-perl_2.111-3_all.deb"],
)

http_file(
    name = "libnet-http-perl_6.18-1_all",
    downloaded_file_path = "libnet-http-perl_6.18-1_all.deb",
    sha256 = "969df027c47d301ba830fce2c802eb314bc5f83e440a922115bd508159f82184",
    urls = ["http://deb.debian.org/debian/pool/main/libn/libnet-http-perl/libnet-http-perl_6.18-1_all.deb"],
)

http_file(
    name = "libtry-tiny-perl_0.30-1_all",
    downloaded_file_path = "libtry-tiny-perl_0.30-1_all.deb",
    sha256 = "bd36c77088f90a876f0cccbbaf28ac0ebc299198022b1451abeb35c3371f878c",
    urls = ["http://deb.debian.org/debian/pool/main/libt/libtry-tiny-perl/libtry-tiny-perl_0.30-1_all.deb"],
)

http_file(
    name = "libwww-robotrules-perl_6.02-1_all",
    downloaded_file_path = "libwww-robotrules-perl_6.02-1_all.deb",
    sha256 = "be69cda8c2a860e64c43396bf2ff1c7145259cb85753ded14e0434f15ed647a0",
    urls = ["http://deb.debian.org/debian/pool/main/libw/libwww-robotrules-perl/libwww-robotrules-perl_6.02-1_all.deb"],
)

http_file(
    name = "libwww-perl_6.36-2_all",
    downloaded_file_path = "libwww-perl_6.36-2_all.deb",
    sha256 = "cae2859bfa6672e614b0bcf2062aa0eea06a9aa6456b0eb6e5fc0ac40ed4320b",
    urls = ["http://deb.debian.org/debian/pool/main/libw/libwww-perl/libwww-perl_6.36-2_all.deb"],
)

http_file(
    name = "liblwp-protocol-https-perl_6.07-2_all",
    downloaded_file_path = "liblwp-protocol-https-perl_6.07-2_all.deb",
    sha256 = "258b3d181756c45ad806e711cd9a08918296139f2a5ddf4040870794bcc4dc37",
    urls = ["http://deb.debian.org/debian/pool/main/libl/liblwp-protocol-https-perl/liblwp-protocol-https-perl_6.07-2_all.deb"],
)

http_file(
    name = "libnet-smtp-ssl-perl_1.04-1_all",
    downloaded_file_path = "libnet-smtp-ssl-perl_1.04-1_all.deb",
    sha256 = "cf23f2c340b048177ef3060644ec759a9002932f8c97889089d741723f8ada6c",
    urls = ["http://deb.debian.org/debian/pool/main/libn/libnet-smtp-ssl-perl/libnet-smtp-ssl-perl_1.04-1_all.deb"],
)

http_file(
    name = "libmailtools-perl_2.18-1_all",
    downloaded_file_path = "libmailtools-perl_2.18-1_all.deb",
    sha256 = "14554e432c6b7bbac113902dde7f1b755aa49ad0a9a7e4d7264a950bc214ecb3",
    urls = ["http://deb.debian.org/debian/pool/main/libm/libmailtools-perl/libmailtools-perl_2.18-1_all.deb"],
)

http_file(
    name = "libxml-parser-perl_2.44-4_amd64",
    downloaded_file_path = "libxml-parser-perl_2.44-4_amd64.deb",
    sha256 = "8edf7cd9db7d296e4e2152e14f1cd97a40ce1f4e60b96ed648d912ea30c8c918",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxml-parser-perl/libxml-parser-perl_2.44-4_amd64.deb"],
)

http_file(
    name = "libxml-twig-perl_3.50-1.1_all",
    downloaded_file_path = "libxml-twig-perl_3.50-1.1_all.deb",
    sha256 = "cebb52e409370ecc35a89b2fc02edcecf8965218b8dd751bb69207b883f05bb9",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxml-twig-perl/libxml-twig-perl_3.50-1.1_all.deb"],
)

http_file(
    name = "libnet-dbus-perl_1.1.0-5b1_amd64",
    downloaded_file_path = "libnet-dbus-perl_1.1.0-5+b1_amd64.deb",
    sha256 = "49bcd38578a52843646eac71d8301db516d786e41e729eac066b3aa8593f496b",
    urls = ["http://deb.debian.org/debian/pool/main/libn/libnet-dbus-perl/libnet-dbus-perl_1.1.0-5+b1_amd64.deb"],
)

http_file(
    name = "libopts25_5.18.12-4_amd64",
    downloaded_file_path = "libopts25_5.18.12-4_amd64.deb",
    sha256 = "129d3a7fdcbcbd551785569da53d26ec73933f839d8c32ac9810cf7e1f7fc902",
    urls = ["http://deb.debian.org/debian/pool/main/a/autogen/libopts25_5.18.12-4_amd64.deb"],
)

http_file(
    name = "libpam-cap_2.25-2_amd64",
    downloaded_file_path = "libpam-cap_2.25-2_amd64.deb",
    sha256 = "06cda4b8cb87c827beb3872e5f889c00ef66bad2971ffd4f7527d624bfcac3e7",
    urls = ["http://deb.debian.org/debian/pool/main/libc/libcap2/libpam-cap_2.25-2_amd64.deb"],
)

http_file(
    name = "libpython3.7_3.7.3-2deb10u3_amd64",
    downloaded_file_path = "libpython3.7_3.7.3-2+deb10u3_amd64.deb",
    sha256 = "ff6f99690d9d85bee5876b53e52248f1ead1a646c5d1beb8f99195d0f43bc260",
    urls = ["http://deb.debian.org/debian/pool/main/p/python3.7/libpython3.7_3.7.3-2+deb10u3_amd64.deb"],
)

http_file(
    name = "libsm6_1.2.3-1_amd64",
    downloaded_file_path = "libsm6_1.2.3-1_amd64.deb",
    sha256 = "22a420890489023346f30fecef14ea900a0788e7bf959ef826aabb83944fccfb",
    urls = ["http://deb.debian.org/debian/pool/main/libs/libsm/libsm6_1.2.3-1_amd64.deb"],
)

http_file(
    name = "libssh-4_0.9.5-1deb11u1_amd64.deb",
    downloaded_file_path = "libssh-4_0.9.5-1+deb11u1_amd64.deb",
    sha256 = "75d04fac5fbea0f4c1f047198122e26aa26f4267de0a3c079c44100b3ea9066f",
    urls = ["http://ftp.us.debian.org/debian/pool/main/libs/libssh/libssh-4_0.9.5-1+deb11u1_amd64.deb"],
)

http_file(
    name = "libtext-iconv-perl_1.7-5b7_amd64",
    downloaded_file_path = "libtext-iconv-perl_1.7-5+b7_amd64.deb",
    sha256 = "c9193a375cbe39a4c00980213a9a169a4dd06802e412b36d9176e0104023afa6",
    urls = ["http://deb.debian.org/debian/pool/main/libt/libtext-iconv-perl/libtext-iconv-perl_1.7-5+b7_amd64.deb"],
)

http_file(
    name = "libtie-ixhash-perl_1.23-2_all",
    downloaded_file_path = "libtie-ixhash-perl_1.23-2_all.deb",
    sha256 = "04a1c1892062a496622635a506eeaf9872c10dfcdd165fe538148d619dc88ecb",
    urls = ["http://deb.debian.org/debian/pool/main/libt/libtie-ixhash-perl/libtie-ixhash-perl_1.23-2_all.deb"],
)

http_file(
    name = "libx11-protocol-perl_0.56-7_all",
    downloaded_file_path = "libx11-protocol-perl_0.56-7_all.deb",
    sha256 = "cf919a9cbc5e91e30cb489bbbd3332a68e18faa3155d0cc2d3063d1659c666f8",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libx11-protocol-perl/libx11-protocol-perl_0.56-7_all.deb"],
)

http_file(
    name = "libxt6_1.1.5-1b3_amd64",
    downloaded_file_path = "libxt6_1.1.5-1+b3_amd64.deb",
    sha256 = "5c474aa7c6bef9c8b0a4cf5cb9102c29ba8c5b2b19a59269ab6e2f0a47a5ec59",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxt/libxt6_1.1.5-1+b3_amd64.deb"],
)

http_file(
    name = "libxmu6_1.1.2-2b3_amd64",
    downloaded_file_path = "libxmu6_1.1.2-2+b3_amd64.deb",
    sha256 = "912a1bfb3416f18193824a4ffc5fe8a3a6e6781d9f8e50e26400dd36a7ca5bd0",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxmu/libxmu6_1.1.2-2+b3_amd64.deb"],
)

http_file(
    name = "libxpm4_3.5.12-1_amd64",
    downloaded_file_path = "libxpm4_3.5.12-1_amd64.deb",
    sha256 = "49e64f0923cdecb2aaf6c93f176c25f63b841da2a501651ae23070f998967aa7",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxpm/libxpm4_3.5.12-1_amd64.deb"],
)

http_file(
    name = "libxaw7_1.0.13-1b2_amd64",
    downloaded_file_path = "libxaw7_1.0.13-1+b2_amd64.deb",
    sha256 = "00238151437d6e3458af3b9b8e7a4b35c3d56bad352ef9e345791ea0876f9e2b",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxaw/libxaw7_1.0.13-1+b2_amd64.deb"],
)

http_file(
    name = "libxcb-shape0_1.13.1-2_amd64",
    downloaded_file_path = "libxcb-shape0_1.13.1-2_amd64.deb",
    sha256 = "971be06832051730a59ef0db4ed49f49efa0539d36f5acf5d1ee0a6e67a1e3bf",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxcb/libxcb-shape0_1.13.1-2_amd64.deb"],
)

http_file(
    name = "libxft2_2.3.2-2_amd64",
    downloaded_file_path = "libxft2_2.3.2-2_amd64.deb",
    sha256 = "cd71384b4d511cba69bcee29af326943c7ca12450765f44c40d246608c779aad",
    urls = ["http://deb.debian.org/debian/pool/main/x/xft/libxft2_2.3.2-2_amd64.deb"],
)

http_file(
    name = "libxml-xpathengine-perl_0.14-1_all",
    downloaded_file_path = "libxml-xpathengine-perl_0.14-1_all.deb",
    sha256 = "3d9a4d3edd4049dcbe09fd84163532744e5c452fafb1ccd4fd08f812c6d82118",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxml-xpathengine-perl/libxml-xpathengine-perl_0.14-1_all.deb"],
)

http_file(
    name = "libxmuu1_1.1.2-2b3_amd64",
    downloaded_file_path = "libxmuu1_1.1.2-2+b3_amd64.deb",
    sha256 = "329d2974829d0dd61ea5d59676ae6db10e49f97588ddff74042edd0a60b3bcd2",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxmu/libxmuu1_1.1.2-2+b3_amd64.deb"],
)

http_file(
    name = "libxv1_1.0.11-1_amd64",
    downloaded_file_path = "libxv1_1.0.11-1_amd64.deb",
    sha256 = "27c1d7435d02c0e9a7d831b290141997c957c78379857b63f6e2a0589456cea5",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxv/libxv1_1.0.11-1_amd64.deb"],
)

http_file(
    name = "libxxf86dga1_1.1.4-1b3_amd64",
    downloaded_file_path = "libxxf86dga1_1.1.4-1+b3_amd64.deb",
    sha256 = "ffe9751a60da2154a226edd0e03af055d6280ad9f00e18ec78c02ab63df27339",
    urls = ["http://deb.debian.org/debian/pool/main/libx/libxxf86dga/libxxf86dga1_1.1.4-1+b3_amd64.deb"],
)

http_file(
    name = "ntp_4.2.8p12dfsg-4_amd64",
    downloaded_file_path = "ntp_4.2.8p12+dfsg-4_amd64.deb",
    sha256 = "cccb2627abb6881a46b4ac2634dfa59126c07bacc8cb175e5568dfa2ee54bdd8",
    urls = ["http://deb.debian.org/debian/pool/main/n/ntp/ntp_4.2.8p12+dfsg-4_amd64.deb"],
)

http_file(
    name = "python3-ntp_1.1.3dfsg1-2deb10u1_amd64",
    downloaded_file_path = "python3-ntp_1.1.3+dfsg1-2+deb10u1_amd64.deb",
    sha256 = "b9b5c52dc06623898df37fd19567c2dd4092361b63343d34b7f4efc48d21ca3b",
    urls = ["http://deb.debian.org/debian/pool/main/n/ntpsec/python3-ntp_1.1.3+dfsg1-2+deb10u1_amd64.deb"],
)

http_file(
    name = "sntp_4.2.8p12dfsg-4_amd64",
    downloaded_file_path = "sntp_4.2.8p12+dfsg-4_amd64.deb",
    sha256 = "7eb6b8186471175d8a5af74de0a28c41e8383a554c8ca28691c7b2c29cdb03f8",
    urls = ["http://deb.debian.org/debian/pool/main/n/ntp/sntp_4.2.8p12+dfsg-4_amd64.deb"],
)

http_file(
    name = "ttf-bitstream-vera_1.10-8_all",
    downloaded_file_path = "ttf-bitstream-vera_1.10-8_all.deb",
    sha256 = "328def7f581bf94b3b06d21e641f3e5df9a9b2e84e93b4206bc952fe8e80f38a",
    urls = ["http://deb.debian.org/debian/pool/main/t/ttf-bitstream-vera/ttf-bitstream-vera_1.10-8_all.deb"],
)

http_file(
    name = "libglx0_1.1.0-1_amd64",
    downloaded_file_path = "libglx0_1.1.0-1_amd64.deb",
    sha256 = "cd370a004c0ddec213b34423963e74c98420f08d45c1dec8f4355ff6c0e9d905",
    urls = ["http://deb.debian.org/debian/pool/main/libg/libglvnd/libglx0_1.1.0-1_amd64.deb"],
)

http_file(
    name = "libgl1_1.1.0-1_amd64",
    downloaded_file_path = "libgl1_1.1.0-1_amd64.deb",
    sha256 = "79420dd0cdb5b9dab9d3266c8c052036c93e363708e27738871692e0e163e5a2",
    urls = ["http://deb.debian.org/debian/pool/main/libg/libglvnd/libgl1_1.1.0-1_amd64.deb"],
)

http_file(
    name = "x11-utils_7.74_amd64",
    downloaded_file_path = "x11-utils_7.7+4_amd64.deb",
    sha256 = "e90ca665c8a28756700aa42fedc6a633be345eaef4807883a47ae1386d362ede",
    urls = ["http://deb.debian.org/debian/pool/main/x/x11-utils/x11-utils_7.7+4_amd64.deb"],
)

http_file(
    name = "x11-xserver-utils_7.78_amd64",
    downloaded_file_path = "x11-xserver-utils_7.7+8_amd64.deb",
    sha256 = "a0873634f63acbdb0811b0cd07a86e9f71b2c5caf207c1b223b70801233f0055",
    urls = ["http://deb.debian.org/debian/pool/main/x/x11-xserver-utils/x11-xserver-utils_7.7+8_amd64.deb"],
)

http_file(
    name = "xdg-user-dirs_0.17-2_amd64",
    downloaded_file_path = "xdg-user-dirs_0.17-2_amd64.deb",
    sha256 = "f499fb0f97de183cfd34a5f03de9c5429fcedd74cc2035534ee4189f2e57b6e3",
    urls = ["http://deb.debian.org/debian/pool/main/x/xdg-user-dirs/xdg-user-dirs_0.17-2_amd64.deb"],
)

http_file(
    name = "xdg-utils_1.1.3-1deb10u1_all",
    downloaded_file_path = "xdg-utils_1.1.3-1+deb10u1_all.deb",
    sha256 = "542f546debc32b2b09f2173decdec1e0040a6f826df9c63af65ab6c6f54d01d0",
    urls = ["http://deb.debian.org/debian/pool/main/x/xdg-utils/xdg-utils_1.1.3-1+deb10u1_all.deb"],
)

http_file(
    name = "coreutils_8.30-3_amd64",
    downloaded_file_path = "coreutils_8.30-3_amd64.deb",
    sha256 = "ae6e5cd6e9aaf74d66edded3931a7a6c916625b8b890379189c75574f6856bf4",
    urls = ["http://deb.debian.org/debian/pool/main/c/coreutils/coreutils_8.30-3_amd64.deb"],
)

http_file(
    name = "dpkg_1.19.7_amd64",
    downloaded_file_path = "dpkg_1.19.7_amd64.deb",
    sha256 = "4eaa78124b0a5495fd06afefc79f7c96a7f7795c6b6a45349ac4173b0c9b7362",
    urls = ["http://deb.debian.org/debian/pool/main/d/dpkg/dpkg_1.19.7_amd64.deb"],
)

http_file(
    name = "login_4.5-1.1_amd64",
    downloaded_file_path = "login_4.5-1.1_amd64.deb",
    sha256 = "fa2d9a8686e5a1d0fef5d7f4c3a1bbd9cb69ee0fa506eec8c6c5528ababe7905",
    urls = ["http://deb.debian.org/debian/pool/main/s/shadow/login_4.5-1.1_amd64.deb"],
)

http_file(
    name = "perl-base_5.28.1-6deb10u1_amd64",
    downloaded_file_path = "perl-base_5.28.1-6+deb10u1_amd64.deb",
    sha256 = "41191859a20c49a1f629ecd8899576631858676c4079f7bbff3808f5376e3680",
    urls = ["http://deb.debian.org/debian/pool/main/p/perl/perl-base_5.28.1-6+deb10u1_amd64.deb"],
)

http_file(
    name = "tar_1.30dfsg-6_amd64",
    downloaded_file_path = "tar_1.30+dfsg-6_amd64.deb",
    sha256 = "8afffcf03195b06b0345a81b307d662fb9419c5795e238ccc5b36eceea3ec22f",
    urls = ["http://deb.debian.org/debian/pool/main/t/tar/tar_1.30+dfsg-6_amd64.deb"],
)

http_file(
    name = "util-linux_2.33.1-0.1_amd64",
    downloaded_file_path = "util-linux_2.33.1-0.1_amd64.deb",
    sha256 = "cb773a92b1ad145d3ab8ff91f037c3aa48a7b2ec52a1fa47a441385eee840ce8",
    urls = ["http://deb.debian.org/debian/pool/main/u/util-linux/util-linux_2.33.1-0.1_amd64.deb"],
)

http_file(
    name = "init-system-helpers_1.56nmu1_all",
    downloaded_file_path = "init-system-helpers_1.56+nmu1_all.deb",
    sha256 = "c457150e2faa01e6677a1d91aa76a868d2b7114deb17ade2ccc2b99235310805",
    urls = ["http://deb.debian.org/debian/pool/main/i/init-system-helpers/init-system-helpers_1.56+nmu1_all.deb"],
)

http_file(
    name = "debconf_1.5.71deb10u1_all",
    downloaded_file_path = "debconf_1.5.71+deb10u1_all.deb",
    sha256 = "768387e57225fdc33ac24e94da8437303e0d52730a22bf38e4bbfa88ef5b6024",
    urls = ["http://deb.debian.org/debian/pool/main/d/debconf/debconf_1.5.71+deb10u1_all.deb"],
)

http_file(
    name = "libpam-modules_1.3.1-5_amd64",
    downloaded_file_path = "libpam-modules_1.3.1-5_amd64.deb",
    sha256 = "bc8a1c2e17c0855a3ecef398299d88696ed6d8254cc03cce3800c4a4063f7d7d",
    urls = ["http://deb.debian.org/debian/pool/main/p/pam/libpam-modules_1.3.1-5_amd64.deb"],
)

http_file(
    name = "libsystemd0_241-7deb10u8_amd64",
    downloaded_file_path = "libsystemd0_241-7~deb10u8_amd64.deb",
    sha256 = "fadce8dbc36955ac93ece6ab2516f927c85480df9419a578c95c388834b4980e",
    urls = ["http://deb.debian.org/debian/pool/main/s/systemd/libsystemd0_241-7~deb10u8_amd64.deb"],
)

http_file(
    name = "mount_2.33.1-0.1_amd64",
    downloaded_file_path = "mount_2.33.1-0.1_amd64.deb",
    sha256 = "cab13d70da8392bd00edbbcb046cf6d9e6791935ff2865836f30b7e595bd6fb0",
    urls = ["http://deb.debian.org/debian/pool/main/u/util-linux/mount_2.33.1-0.1_amd64.deb"],
)

http_file(
    name = "libpam-modules-bin_1.3.1-5_amd64",
    downloaded_file_path = "libpam-modules-bin_1.3.1-5_amd64.deb",
    sha256 = "9ba6ca27c6d4077846c2ec3489c30b8d699391393fa0c0de28a1de8cffbf118e",
    urls = ["http://deb.debian.org/debian/pool/main/p/pam/libpam-modules-bin_1.3.1-5_amd64.deb"],
)

http_file(
    name = "libpam-runtime_1.3.1-5_all",
    downloaded_file_path = "libpam-runtime_1.3.1-5_all.deb",
    sha256 = "8aedc549e97e42fd21429d82a18ead489399ff4c15c6c688cdedea591eed9a66",
    urls = ["http://deb.debian.org/debian/pool/main/p/pam/libpam-runtime_1.3.1-5_all.deb"],
)

http_file(
    name = "passwd_4.5-1.1_amd64",
    downloaded_file_path = "passwd_4.5-1.1_amd64.deb",
    sha256 = "23af4a550da375cefbac02484e49ed1c2e6717c0f76533137b3f2fa2cc277cf2",
    urls = ["http://deb.debian.org/debian/pool/main/s/shadow/passwd_4.5-1.1_amd64.deb"],
)

http_file(
    name = "adduser_3.118_all",
    downloaded_file_path = "adduser_3.118_all.deb",
    sha256 = "bd71dd1ab8dcd6005390708f23741d07f1913877affb7604dfd55f85d009aa2b",
    urls = ["http://deb.debian.org/debian/pool/main/a/adduser/adduser_3.118_all.deb"],
)

http_file(
    name = "libacl1_2.2.53-4_amd64",
    downloaded_file_path = "libacl1_2.2.53-4_amd64.deb",
    sha256 = "ca1b512a4a09317018408bbb65ee3f48abdf03dcb8da671554a1f2bd8e5d4de4",
    urls = ["http://deb.debian.org/debian/pool/main/a/acl/libacl1_2.2.53-4_amd64.deb"],
)

http_file(
    name = "libattr1_2.4.48-4_amd64",
    downloaded_file_path = "libattr1_2.4.48-4_amd64.deb",
    sha256 = "4ba903c087f2b9661e067ca210cfd83ef2dc1a162a15b8735997bfa96ac8e760",
    urls = ["http://deb.debian.org/debian/pool/main/a/attr/libattr1_2.4.48-4_amd64.deb"],
)

http_file(
    name = "libaudit-common_2.8.4-3_all",
    downloaded_file_path = "libaudit-common_2.8.4-3_all.deb",
    sha256 = "4e51dc247cde083528d410f525c6157b08be8b69511891cf972bc87025311371",
    urls = ["http://deb.debian.org/debian/pool/main/a/audit/libaudit-common_2.8.4-3_all.deb"],
)

http_file(
    name = "libaudit1_2.8.4-3_amd64",
    downloaded_file_path = "libaudit1_2.8.4-3_amd64.deb",
    sha256 = "21f2b3dfbe7db9e15ff9c01e1ad8db35a0adf41b70a3aa71e809f7631fc2253d",
    urls = ["http://deb.debian.org/debian/pool/main/a/audit/libaudit1_2.8.4-3_amd64.deb"],
)

http_file(
    name = "libblkid1_2.33.1-0.1_amd64",
    downloaded_file_path = "libblkid1_2.33.1-0.1_amd64.deb",
    sha256 = "0b15f3eb3cf2fbe540f99ae1c9fd5ec1730f2245b99e31c91755de71b967343a",
    urls = ["http://deb.debian.org/debian/pool/main/u/util-linux/libblkid1_2.33.1-0.1_amd64.deb"],
)

http_file(
    name = "libbz2-1.0_1.0.6-9.2deb10u1_amd64",
    downloaded_file_path = "libbz2-1.0_1.0.6-9.2~deb10u1_amd64.deb",
    sha256 = "238193cbaa71cc5365ef2aa5ad45de8521ac38dd54f4ab53bafa7de15046fa89",
    urls = ["http://deb.debian.org/debian/pool/main/b/bzip2/libbz2-1.0_1.0.6-9.2~deb10u1_amd64.deb"],
)

http_file(
    name = "libc6_2.28-10_amd64",
    downloaded_file_path = "libc6_2.28-10_amd64.deb",
    sha256 = "6f703e27185f594f8633159d00180ea1df12d84f152261b6e88af75667195a79",
    urls = ["http://deb.debian.org/debian/pool/main/g/glibc/libc6_2.28-10_amd64.deb"],
)

http_file(
    name = "libcap-ng0_0.7.9-2_amd64",
    downloaded_file_path = "libcap-ng0_0.7.9-2_amd64.deb",
    sha256 = "4f9caf61638db6dcf79529ef756a2d36c7aae6604d486a5055bb3212d823b691",
    urls = ["http://deb.debian.org/debian/pool/main/libc/libcap-ng/libcap-ng0_0.7.9-2_amd64.deb"],
)

http_file(
    name = "libdb5.3_5.3.28dfsg1-0.5_amd64",
    downloaded_file_path = "libdb5.3_5.3.28+dfsg1-0.5_amd64.deb",
    sha256 = "c7f0e9a423840731362ee52d4344c0bcf84318fbc06dad4fefe0e61d9e7062bc",
    urls = ["http://deb.debian.org/debian/pool/main/d/db5.3/libdb5.3_5.3.28+dfsg1-0.5_amd64.deb"],
)

http_file(
    name = "libgcc1_8.3.0-6_amd64",
    downloaded_file_path = "libgcc1_8.3.0-6_amd64.deb",
    sha256 = "b1bb7611f3372732889d502cb1d09fe572b5fbb5288a4a8b1ed0363fecc3555a",
    urls = ["http://deb.debian.org/debian/pool/main/g/gcc-8/libgcc1_8.3.0-6_amd64.deb"],
)

http_file(
    name = "libgcrypt20_1.8.4-5deb10u1_amd64",
    downloaded_file_path = "libgcrypt20_1.8.4-5+deb10u1_amd64.deb",
    sha256 = "b29220a4042423b5466869c27bc4b10115e2e3a4c43eda80569b7a98ab35af93",
    urls = ["http://deb.debian.org/debian/pool/main/libg/libgcrypt20/libgcrypt20_1.8.4-5+deb10u1_amd64.deb"],
)

http_file(
    name = "libgmp10_6.1.2dfsg-4_amd64",
    downloaded_file_path = "libgmp10_6.1.2+dfsg-4_amd64.deb",
    sha256 = "d9c9661c7d4d686a82c29d183124adacbefff797f1ef5723d509dbaa2e92a87c",
    urls = ["http://deb.debian.org/debian/pool/main/g/gmp/libgmp10_6.1.2+dfsg-4_amd64.deb"],
)

http_file(
    name = "libgnutls30_3.6.7-4deb10u7_amd64",
    downloaded_file_path = "libgnutls30_3.6.7-4+deb10u7_amd64.deb",
    sha256 = "77154ca16973217195371c3136152d6747bd4646bef157ffdbc3f531b707b483",
    urls = ["http://deb.debian.org/debian/pool/main/g/gnutls28/libgnutls30_3.6.7-4+deb10u7_amd64.deb"],
)

http_file(
    name = "libgpg-error0_1.35-1_amd64",
    downloaded_file_path = "libgpg-error0_1.35-1_amd64.deb",
    sha256 = "996b67baf6b5c6fda0db2df27cce15701b122403d0a7f30e9a1f50d07205450a",
    urls = ["http://deb.debian.org/debian/pool/main/libg/libgpg-error/libgpg-error0_1.35-1_amd64.deb"],
)

http_file(
    name = "libidn2-0_2.0.5-1deb10u1_amd64",
    downloaded_file_path = "libidn2-0_2.0.5-1+deb10u1_amd64.deb",
    sha256 = "13c3129c4930cd8b1255dbc5da7068c036f217218d1017634b83847a659fad16",
    urls = ["http://deb.debian.org/debian/pool/main/libi/libidn2/libidn2-0_2.0.5-1+deb10u1_amd64.deb"],
)

http_file(
    name = "liblz4-1_1.8.3-1deb10u1_amd64",
    downloaded_file_path = "liblz4-1_1.8.3-1+deb10u1_amd64.deb",
    sha256 = "557d5f046945714745b02465cb6e718b0dc3ce11298f9722694a10d1498d083d",
    urls = ["http://deb.debian.org/debian/pool/main/l/lz4/liblz4-1_1.8.3-1+deb10u1_amd64.deb"],
)

http_file(
    name = "liblzma5_5.2.4-1_amd64",
    downloaded_file_path = "liblzma5_5.2.4-1_amd64.deb",
    sha256 = "292dfe85defad3a08cca62beba85e90b0231d16345160f4a66aba96399c85859",
    urls = ["http://deb.debian.org/debian/pool/main/x/xz-utils/liblzma5_5.2.4-1_amd64.deb"],
)

http_file(
    name = "libmount1_2.33.1-0.1_amd64",
    downloaded_file_path = "libmount1_2.33.1-0.1_amd64.deb",
    sha256 = "b8b28669dc4995a7a48d47d9199d1806d4fce9c4051277279d4dcc514c086ba3",
    urls = ["http://deb.debian.org/debian/pool/main/u/util-linux/libmount1_2.33.1-0.1_amd64.deb"],
)

http_file(
    name = "libncursesw6_6.120181013-2deb10u2_amd64",
    downloaded_file_path = "libncursesw6_6.1+20181013-2+deb10u2_amd64.deb",
    sha256 = "7dffe9602586300292960f2e3cf4301acfc64a91aed6fa41ea2e719ae75788b3",
    urls = ["http://deb.debian.org/debian/pool/main/n/ncurses/libncursesw6_6.1+20181013-2+deb10u2_amd64.deb"],
)

http_file(
    name = "libp11-kit0_0.23.15-2deb10u1_amd64",
    downloaded_file_path = "libp11-kit0_0.23.15-2+deb10u1_amd64.deb",
    sha256 = "02b2f15ad9cd2ead38dbeb85cdba65d8e4d44155495110231504cd0de1f16f83",
    urls = ["http://deb.debian.org/debian/pool/main/p/p11-kit/libp11-kit0_0.23.15-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libpam0g_1.3.1-5_amd64",
    downloaded_file_path = "libpam0g_1.3.1-5_amd64.deb",
    sha256 = "b480fef838d01dc647170fdbde8d44c12e05e04da989b3bffd44223457cee0dc",
    urls = ["http://deb.debian.org/debian/pool/main/p/pam/libpam0g_1.3.1-5_amd64.deb"],
)

http_file(
    name = "libpcre3_8.39-12_amd64",
    downloaded_file_path = "libpcre3_8.39-12_amd64.deb",
    sha256 = "5496ea46b812b1a00104fc97b30e13fc5f8f6e9ec128a8ff4fd2d66a80cc6bee",
    urls = ["http://deb.debian.org/debian/pool/main/p/pcre3/libpcre3_8.39-12_amd64.deb"],
)

http_file(
    name = "libseccomp2_2.3.3-4_amd64",
    downloaded_file_path = "libseccomp2_2.3.3-4_amd64.deb",
    sha256 = "0d0fcfc610a7a7023d55835e55ddcf625b760d195ef259fb137afff9ba4f2e9d",
    urls = ["http://deb.debian.org/debian/pool/main/libs/libseccomp/libseccomp2_2.3.3-4_amd64.deb"],
)

http_file(
    name = "libselinux1_2.8-1b1_amd64",
    downloaded_file_path = "libselinux1_2.8-1+b1_amd64.deb",
    sha256 = "05238a8c13c32418511a965e7b756ab031c140ef154ca0b3b2a1bb7a14e2faab",
    urls = ["http://deb.debian.org/debian/pool/main/libs/libselinux/libselinux1_2.8-1+b1_amd64.deb"],
)

http_file(
    name = "libsemanage-common_2.8-2_all",
    downloaded_file_path = "libsemanage-common_2.8-2_all.deb",
    sha256 = "fa3c50e11afa9250f823218898084bdefea73c7cd1995ef5ed5e7c12e7b46331",
    urls = ["http://deb.debian.org/debian/pool/main/libs/libsemanage/libsemanage-common_2.8-2_all.deb"],
)

http_file(
    name = "libsemanage1_2.8-2_amd64",
    downloaded_file_path = "libsemanage1_2.8-2_amd64.deb",
    sha256 = "ebc5346a40336fb481865e48a2a5356b5124fc868269dc2c1fbab2bdc2ac495e",
    urls = ["http://deb.debian.org/debian/pool/main/libs/libsemanage/libsemanage1_2.8-2_amd64.deb"],
)

http_file(
    name = "libsepol1_2.8-1_amd64",
    downloaded_file_path = "libsepol1_2.8-1_amd64.deb",
    sha256 = "5e4ebf890bab2422d3caff579006c02cc3b153e98a61b8c548a951e24c0693f2",
    urls = ["http://deb.debian.org/debian/pool/main/libs/libsepol/libsepol1_2.8-1_amd64.deb"],
)

http_file(
    name = "libsmartcols1_2.33.1-0.1_amd64",
    downloaded_file_path = "libsmartcols1_2.33.1-0.1_amd64.deb",
    sha256 = "9a83a75545de917ada2d8a3a1aaff88e6897a873637784b0c1b45fbb93459ff4",
    urls = ["http://deb.debian.org/debian/pool/main/u/util-linux/libsmartcols1_2.33.1-0.1_amd64.deb"],
)

http_file(
    name = "libstdc6_8.3.0-6_amd64",
    downloaded_file_path = "libstdc++6_8.3.0-6_amd64.deb",
    sha256 = "5cc70625329655ff9382580971d4616db8aa39af958b7c995ee84598f142a4ee",
    urls = ["http://deb.debian.org/debian/pool/main/g/gcc-8/libstdc++6_8.3.0-6_amd64.deb"],
)

http_file(
    name = "libtasn1-6_4.13-3_amd64",
    downloaded_file_path = "libtasn1-6_4.13-3_amd64.deb",
    sha256 = "2771ea1ba49d30f033e67e708f71da9b031649c7c13d2ce04cb3ec913ac3b839",
    urls = ["http://deb.debian.org/debian/pool/main/libt/libtasn1-6/libtasn1-6_4.13-3_amd64.deb"],
)

http_file(
    name = "libtinfo6_6.120181013-2deb10u2_amd64",
    downloaded_file_path = "libtinfo6_6.1+20181013-2+deb10u2_amd64.deb",
    sha256 = "7f39c7a7b02c3373a427aa276830a6e1e0c4cc003371f34e2e50e9992aa70e1a",
    urls = ["http://deb.debian.org/debian/pool/main/n/ncurses/libtinfo6_6.1+20181013-2+deb10u2_amd64.deb"],
)

http_file(
    name = "libudev1_241-7deb10u8_amd64",
    downloaded_file_path = "libudev1_241-7~deb10u8_amd64.deb",
    sha256 = "18a81cef276a3af9f5ff61fef4771518b78f3738a051195dafd301bfb7815b29",
    urls = ["http://deb.debian.org/debian/pool/main/s/systemd/libudev1_241-7~deb10u8_amd64.deb"],
)

http_file(
    name = "libunistring2_0.9.10-1_amd64",
    downloaded_file_path = "libunistring2_0.9.10-1_amd64.deb",
    sha256 = "bc3961271c9f78e7ef93dec3bf7c1047f2cde73dfc3e2b0c475b6115b76780f8",
    urls = ["http://deb.debian.org/debian/pool/main/libu/libunistring/libunistring2_0.9.10-1_amd64.deb"],
)

http_file(
    name = "libuuid1_2.33.1-0.1_amd64",
    downloaded_file_path = "libuuid1_2.33.1-0.1_amd64.deb",
    sha256 = "90b90bef4593d4f347fb1e74a63c5609daa86d4c5003b14e85f58628d6c118b2",
    urls = ["http://deb.debian.org/debian/pool/main/u/util-linux/libuuid1_2.33.1-0.1_amd64.deb"],
)

http_file(
    name = "libzstd1_1.3.8dfsg-3deb10u2_amd64",
    downloaded_file_path = "libzstd1_1.3.8+dfsg-3+deb10u2_amd64.deb",
    sha256 = "3c1d6fdaeb4a2357c27b3ac7a488eee562fef2961972098be386b5618e6d20ce",
    urls = ["http://deb.debian.org/debian/pool/main/libz/libzstd/libzstd1_1.3.8+dfsg-3+deb10u2_amd64.deb"],
)

http_file(
    name = "zlib1g_1.2.11.dfsg-1_amd64",
    downloaded_file_path = "zlib1g_1.2.11.dfsg-1_amd64.deb",
    sha256 = "61bc9085aadd3007433ce6f560a08446a3d3ceb0b5e061db3fc62c42fbfe3eff",
    urls = ["http://deb.debian.org/debian/pool/main/z/zlib/zlib1g_1.2.11.dfsg-1_amd64.deb"],
)

http_file(
    name = "tzdata_2021a-0deb10u2_all",
    downloaded_file_path = "tzdata_2021a-0+deb10u2_all.deb",
    sha256 = "df0982c75ccefa2268e77b0c2baba1dd2440bb2a03a9f1fcecebc2f6fce9171f",
    urls = ["http://deb.debian.org/debian/pool/main/t/tzdata/tzdata_2021a-0+deb10u2_all.deb"],
)

http_file(
    name = "readline-common_7.0-5_all",
    downloaded_file_path = "readline-common_7.0-5_all.deb",
    sha256 = "153d8a5ddb04044d10f877a8955d944612ec9035f4c73eec99d85a92c3816712",
    urls = ["http://deb.debian.org/debian/pool/main/r/readline/readline-common_7.0-5_all.deb"],
)

http_file(
    name = "sensible-utils_0.0.12_all",
    downloaded_file_path = "sensible-utils_0.0.12_all.deb",
    sha256 = "2043859f8bf39a20d075bf52206549f90dcabd66665bb9d6837273494fc6a598",
    urls = ["http://deb.debian.org/debian/pool/main/s/sensible-utils/sensible-utils_0.0.12_all.deb"],
)

http_file(
    name = "ucf_3.0038nmu1_all",
    downloaded_file_path = "ucf_3.0038+nmu1_all.deb",
    sha256 = "d02a82455faab988a52121f37d97c528a4f967ed75e9398e1d8db571398c12f9",
    urls = ["http://deb.debian.org/debian/pool/main/u/ucf/ucf_3.0038+nmu1_all.deb"],
)

http_file(
    name = "wget_1.20.1-1.1_amd64",
    downloaded_file_path = "wget_1.20.1-1.1_amd64.deb",
    sha256 = "3821cee0d331cf75ee79daff716f9d320f758f9dff3eaa6d6cf12bae9ef14306",
    urls = ["http://deb.debian.org/debian/pool/main/w/wget/wget_1.20.1-1.1_amd64.deb"],
)

http_file(
    name = "ca-certificates_20200601deb10u2_all",
    downloaded_file_path = "ca-certificates_20200601~deb10u2_all.deb",
    sha256 = "a9e267a24088c793a9cf782455fd344db5fdced714f112a8857c5bfd07179387",
    urls = ["http://deb.debian.org/debian/pool/main/c/ca-certificates/ca-certificates_20200601~deb10u2_all.deb"],
)

http_file(
    name = "fontconfig_2.13.1-2_amd64",
    downloaded_file_path = "fontconfig_2.13.1-2_amd64.deb",
    sha256 = "efbc7d9a8cf245e31429d3bda3e560df275f6b7302367aabe83503ca734ac0fd",
    urls = ["http://deb.debian.org/debian/pool/main/f/fontconfig/fontconfig_2.13.1-2_amd64.deb"],
)

http_file(
    name = "fontconfig-config_2.13.1-2_all",
    downloaded_file_path = "fontconfig-config_2.13.1-2_all.deb",
    sha256 = "9f5d34ba20eb156ef62d8126866a376be985c6a83fdcfb33f12cd83acac480c2",
    urls = ["http://deb.debian.org/debian/pool/main/f/fontconfig/fontconfig-config_2.13.1-2_all.deb"],
)

http_file(
    name = "fonts-dejavu-core_2.37-1_all",
    downloaded_file_path = "fonts-dejavu-core_2.37-1_all.deb",
    sha256 = "58d21a255606191e6512cca51f32c4480e7a798945cc980623377696acfa3cfc",
    urls = ["http://deb.debian.org/debian/pool/main/f/fonts-dejavu/fonts-dejavu-core_2.37-1_all.deb"],
)

http_file(
    name = "libcom-err2_1.44.5-1deb10u3_amd64",
    downloaded_file_path = "libcom-err2_1.44.5-1+deb10u3_amd64.deb",
    sha256 = "e5ea8e6db9453ed13199f4cbfe8e29d76c579eb6f678ab9bb4bebd7d12c1936e",
    urls = ["http://deb.debian.org/debian/pool/main/e/e2fsprogs/libcom-err2_1.44.5-1+deb10u3_amd64.deb"],
)

http_file(
    name = "libexpat1_2.2.6-2deb10u1_amd64",
    downloaded_file_path = "libexpat1_2.2.6-2+deb10u1_amd64.deb",
    sha256 = "d60dee1f402ee0fba6d44df584512ae9ede73e866048e8476de55d9b78fa2da1",
    urls = ["http://deb.debian.org/debian/pool/main/e/expat/libexpat1_2.2.6-2+deb10u1_amd64.deb"],
)

http_file(
    name = "libfontconfig1_2.13.1-2_amd64",
    downloaded_file_path = "libfontconfig1_2.13.1-2_amd64.deb",
    sha256 = "6766d0bcfc615fb15542efb5235d38237ccaec4c219beb84dbd22d1662ccea8f",
    urls = ["http://deb.debian.org/debian/pool/main/f/fontconfig/libfontconfig1_2.13.1-2_amd64.deb"],
)

http_file(
    name = "libfreetype6_2.9.1-3deb10u2_amd64",
    downloaded_file_path = "libfreetype6_2.9.1-3+deb10u2_amd64.deb",
    sha256 = "93f009440fd1ffcc4b3afdbc413eccc1d8101145a262ca0d0c305fc7029f2417",
    urls = ["http://deb.debian.org/debian/pool/main/f/freetype/libfreetype6_2.9.1-3+deb10u2_amd64.deb"],
)

http_file(
    name = "libldap-2.4-2_2.4.47dfsg-3deb10u6_amd64",
    downloaded_file_path = "libldap-2.4-2_2.4.47+dfsg-3+deb10u6_amd64.deb",
    sha256 = "246b2f152d29ca1552b8c422721f0726fcc8a0b1c50b04ac99bf3c42c198e742",
    urls = ["http://deb.debian.org/debian/pool/main/o/openldap/libldap-2.4-2_2.4.47+dfsg-3+deb10u6_amd64.deb"],
)

http_file(
    name = "libldap-common_2.4.47dfsg-3deb10u6_all",
    downloaded_file_path = "libldap-common_2.4.47+dfsg-3+deb10u6_all.deb",
    sha256 = "ec2bb7b5165d47516cbf40a688352764fd093484ea4e3269349c9e626d2a53de",
    urls = ["http://deb.debian.org/debian/pool/main/o/openldap/libldap-common_2.4.47+dfsg-3+deb10u6_all.deb"],
)

http_file(
    name = "libpcre2-8-0_10.32-5_amd64",
    downloaded_file_path = "libpcre2-8-0_10.32-5_amd64.deb",
    sha256 = "18fa901205ed21c833ff669daae26f675803147f4cc64ddc95fc9cddd7f654c8",
    urls = ["http://deb.debian.org/debian/pool/main/p/pcre2/libpcre2-8-0_10.32-5_amd64.deb"],
)

http_file(
    name = "libpng16-16_1.6.36-6_amd64",
    downloaded_file_path = "libpng16-16_1.6.36-6_amd64.deb",
    sha256 = "82a252478465521cde9d5af473df01ed79f16e912effc5971892a574e9113500",
    urls = ["http://deb.debian.org/debian/pool/main/libp/libpng1.6/libpng16-16_1.6.36-6_amd64.deb"],
)

http_file(
    name = "libpsl5_0.20.2-2_amd64",
    downloaded_file_path = "libpsl5_0.20.2-2_amd64.deb",
    sha256 = "290fc88e99d21586164d51f8562c3b4c6a3bfabdbb626d91b6541896d76a582b",
    urls = ["http://deb.debian.org/debian/pool/main/libp/libpsl/libpsl5_0.20.2-2_amd64.deb"],
)

http_file(
    name = "libsasl2-2_2.1.27dfsg-1deb10u1_amd64",
    downloaded_file_path = "libsasl2-2_2.1.27+dfsg-1+deb10u1_amd64.deb",
    sha256 = "4a3fb6e0953789f3de455ad7c921294978d734e6395bc45bd6039dcd9634d263",
    urls = ["http://deb.debian.org/debian/pool/main/c/cyrus-sasl2/libsasl2-2_2.1.27+dfsg-1+deb10u1_amd64.deb"],
)

http_file(
    name = "libsasl2-modules-db_2.1.27dfsg-1deb10u1_amd64",
    downloaded_file_path = "libsasl2-modules-db_2.1.27+dfsg-1+deb10u1_amd64.deb",
    sha256 = "c99437674b33964f44eb54b1a4d8cb5bbca0293989cd3d426bcb54e9f54d88db",
    urls = ["http://deb.debian.org/debian/pool/main/c/cyrus-sasl2/libsasl2-modules-db_2.1.27+dfsg-1+deb10u1_amd64.deb"],
)

http_file(
    name = "libsqlite3-0_3.27.2-3deb10u1_amd64",
    downloaded_file_path = "libsqlite3-0_3.27.2-3+deb10u1_amd64.deb",
    sha256 = "19268b796e62f754400c67c69cb759220089cf10aaa5dfd72a84ab1a818caa08",
    urls = ["http://deb.debian.org/debian/pool/main/s/sqlite3/libsqlite3-0_3.27.2-3+deb10u1_amd64.deb"],
)

http_file(
    name = "libssl1.1_1.1.1d-0deb10u7_amd64",
    downloaded_file_path = "libssl1.1_1.1.1d-0+deb10u7_amd64.deb",
    sha256 = "49e1171928d3930fb8ba5659a80e8862d7d585c6d750acb6520b1c133ac00b29",
    urls = ["http://deb.debian.org/debian/pool/main/o/openssl/libssl1.1_1.1.1d-0+deb10u7_amd64.deb"],
)

http_file(
    name = "lsb-base_10.2019051400_all",
    downloaded_file_path = "lsb-base_10.2019051400_all.deb",
    sha256 = "2dd69416c4e8decda8a9ed56e36275df7645aea7851b05eb16d42fed61b6a12f",
    urls = ["http://deb.debian.org/debian/pool/main/l/lsb/lsb-base_10.2019051400_all.deb"],
)

http_file(
    name = "openssl_1.1.1d-0deb10u7_amd64",
    downloaded_file_path = "openssl_1.1.1d-0+deb10u7_amd64.deb",
    sha256 = "eb2c128d1b378547bab986c7024bc297573a4ac6f180d2b44465b6ad5b432284",
    urls = ["http://deb.debian.org/debian/pool/main/o/openssl/openssl_1.1.1d-0+deb10u7_amd64.deb"],
)

http_file(
    name = "chrony_3.4-4deb10u1_amd64",
    downloaded_file_path = "chrony_3.4-4+deb10u1_amd64.deb",
    sha256 = "d9e5807e02f184b529bb291dd1a7a19bceba521a9caa0133389bbbad9be42f05",
    urls = ["http://ftp.br.debian.org/debian/pool/main/c/chrony/chrony_3.4-4+deb10u1_amd64.deb"],
)

http_file(
    name = "ntpsec_1.1.3dfsg1-2deb10u1_amd64",
    downloaded_file_path = "ntpsec_1.1.3+dfsg1-2+deb10u1_amd64.deb",
    sha256 = "1cc4cd3f5b7359295669d05d2f247ce27cade1566612bc7346203d0dab99d1d8",
    urls = ["http://ftp.br.debian.org/debian/pool/main/n/ntpsec/ntpsec_1.1.3+dfsg1-2+deb10u1_amd64.deb"],
)

http_file(
    name = "openntpd_6.2p3-4_amd64",
    downloaded_file_path = "openntpd_6.2p3-4_amd64.deb",
    sha256 = "5b4ccdb518718bcc4e63b78759873007b0daa36e510c8ee7f96a5aa55b07a9f8",
    urls = ["http://ftp.br.debian.org/debian/pool/main/o/openntpd/openntpd_6.2p3-4_amd64.deb"],
)

http_file(
    name = "gcc-10-base_10.2.1-6_amd64",
    downloaded_file_path = "gcc-10-base_10.2.1-6_amd64.deb",
    sha256 = "be65535e94f95fbf04b104e8ab36790476f063374430f7dfc6c516cbe2d2cd1e",
    urls = ["http://ftp.us.debian.org/debian/pool/main/g/gcc-10/gcc-10-base_10.2.1-6_amd64.deb"],
)

http_file(
    name = "libcrypt1_4.4.18-4_amd64",
    downloaded_file_path = "libcrypt1_4.4.18-4_amd64.deb",
    sha256 = "f617952df0c57b4ee039448e3941bccd3f97bfff71e9b0f87ca6dae15cb3f5ef",
    urls = ["http://ftp.us.debian.org/debian/pool/main/libx/libxcrypt/libcrypt1_4.4.18-4_amd64.deb"],
)

http_file(
    name = "libffi6_3.2.1-9_amd64",
    downloaded_file_path = "libffi6_3.2.1-9_amd64.deb",
    sha256 = "d4d748d897e8e53aa239ead23a18724a1a30085cc6ca41a8c31b3b1e1b3452f4",
    urls = ["http://ftp.br.debian.org/debian/pool/main/libf/libffi/libffi6_3.2.1-9_amd64.deb"],
)

http_file(
    name = "libgcc-s1_10.2.1-6_amd64",
    downloaded_file_path = "libgcc-s1_10.2.1-6_amd64.deb",
    sha256 = "e478f2709d8474165bb664de42e16950c391f30eaa55bc9b3573281d83a29daf",
    urls = ["http://ftp.us.debian.org/debian/pool/main/g/gcc-10/libgcc-s1_10.2.1-6_amd64.deb"],
)

http_file(
    name = "libip4tc2_1.8.7-1_amd64",
    downloaded_file_path = "libip4tc2_1.8.7-1_amd64.deb",
    sha256 = "7adeb63d9a350794b8234082082608d1b81097f5b177a9d16c28b72584c4f527",
    urls = ["http://ftp.us.debian.org/debian/pool/main/i/iptables/libip4tc2_1.8.7-1_amd64.deb"],
)

http_file(
    name = "libreadline8_8.1-1_amd64",
    downloaded_file_path = "libreadline8_8.1-1_amd64.deb",
    sha256 = "162ba9fdcde81b5502953ed4d84b24e8ad4e380bbd02990ab1a0e3edffca3c22",
    urls = ["http://ftp.us.debian.org/debian/pool/main/r/readline/libreadline8_8.1-1_amd64.deb"],
)

http_file(
    name = "systemd-timesyncd_247.3-6_amd64",
    downloaded_file_path = "systemd-timesyncd_247.3-6_amd64.deb",
    sha256 = "1f547cc3e25d54ef99e461c8cdad7b5d88e2406067df516cbec841c26349bcea",
    urls = ["http://ftp.us.debian.org/debian/pool/main/s/systemd/systemd-timesyncd_247.3-6_amd64.deb"],
)

http_file(
    name = "libnettle6_3.4.1-1deb10u1_amd64",
    downloaded_file_path = "libnettle6_3.4.1-1+deb10u1_amd64.deb",
    sha256 = "e7139151367e9ee82bb7c1664ce5793f8aa9310492604726d0f535f969394bdf",
    urls = ["http://ftp.br.debian.org/debian/pool/main/n/nettle/libnettle6_3.4.1-1+deb10u1_amd64.deb"],
)

http_file(
    name = "libhogweed4_3.4.1-1deb10u1_amd64",
    downloaded_file_path = "libhogweed4_3.4.1-1+deb10u1_amd64.deb",
    sha256 = "e57c058cfd9b6622dd595be4ee94e2aed595be8cbc8d0623db5fb4595f09cdd5",
    urls = ["http://ftp.br.debian.org/debian/pool/main/n/nettle/libhogweed4_3.4.1-1+deb10u1_amd64.deb"],
)

# end of chromium related files

http_archive(
    name = "com_github_grpc_grpc",
    sha256 = "0343e6dbde66e9a31c691f2f61e98d79f3584e03a11511fad3f10e3667832a45",
    strip_prefix = "grpc-1.29.1",
    urls = ["https://github.com/grpc/grpc/archive/refs/tags/v1.29.1.tar.gz"],
)

# Python
http_archive(
    name = "rules_python",
    sha256 = "778197e26c5fbeb07ac2a2c5ae405b30f6cb7ad1f5510ea6fdac03bded96cc6f",
    url = "https://github.com/bazelbuild/rules_python/releases/download/0.2.0/rules_python-0.2.0.tar.gz",
)

# For publishing maven artifacts (required due to usage in tink-backend)
git_repository(
    name = "graknlabs_bazel_distribution",
    commit = "19ec01a93637c5cc180ee523f25b190a79adcc35",
    remote = "https://github.com/graknlabs/bazel-distribution",
)

load("@tink_backend//tools/bzl:junit5.bzl", "junit_jupiter_java_repositories", "junit_platform_java_repositories")

junit_jupiter_java_repositories()

junit_platform_java_repositories()
