load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_bundle",
    "container_image",
    "container_push",
)

container_image(
    name = "aggregation_image",
    base = "@openjdk_jdk8//image",
    cmd = [
        "java",
        "-jar",
        "-Xmx2560m",
        "/usr/share/tink-backend-aggregation/aggregation_service.jar",
        "server",
        "/etc/tink/aggregation-server.yml",
    ],
    workdir = "/usr/share/tink-backend-aggregation",
    tars = [
        "//deb:aggregation_bin_tar",
        "//deb:aggregation_data_tar",
        "//deb:libkbc_wbaes_bin_tar",
        "//deb:phantomjs_bin_tar",
    ],
)

container_push(
    name = "aggregation_push",
    format = "Docker",
    image = ":aggregation_image",
    registry = "gcr.io",
    repository = "tink-containers/tink-backend-aggregation",
    tag = "{TINK_VERSION}",
)

container_image(
    name = "provider_configuration_image",
    base = "@openjdk_jdk8//image",
    cmd = [
        "java",
        "-jar",
        "-Xmx2560m",
        "/usr/share/tink-backend-provider-configuration/provider_configuration_service.jar",
        "server",
        "/etc/tink/provider-configuration-server.yml",
    ],
    tars = [
        "//deb:provider_configuration_bin_tar",
        "//deb:provider_configuration_data_tar",
    ],
    workdir = "/usr/share/tink-backend-provider-configuration",
)

container_push(
    name = "provider_configuration_push",
    format = "Docker",
    image = ":provider_configuration_image",
    registry = "gcr.io",
    repository = "tink-containers/tink-backend-provider-configuration",
    tag = "{TINK_VERSION}",
)

# You need to have python installed in your PATH and python 2 is currently
# required to build the docker images:
#   $ brew install python2
#   $ export PATH="/usr/local/Cellar/python@2/2.7.14_3/bin:$PATH"
#
# The bazel server needs to be restarted to pick up on the changed Python
# version:
#   $ bazel shutdown
#
# If you want to use the images inside Minikube, connect to the Docker daemon
# running on the inside:
#   $ eval $(minikube docker-env)
#
# Currently consumable docker images can be produced by running:
#   $ bazel build docker:bundle.tar
#   $ docker load -i bazel-bin/docker/bundle.tar
#
container_bundle(
    name = "bundle",
    images = {
        "gcr.io/tink-containers/tink-backend-aggregation:latest": ":aggregation_image",
        "gcr.io/tink-containers/tink-backend-provider-configuration:latest": ":provider_configuration_image",
    },
)
