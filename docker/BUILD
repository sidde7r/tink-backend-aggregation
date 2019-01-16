load(
    "@io_bazel_rules_docker//container:container.bzl",
     "container_bundle", "container_image",
)

container_image(
    name = "aggregation_image",
    base = "@openjdk-jdk8//image",
    tars = ["//deb:aggregation_tar"],
    cmd = ["java", "-jar", "-Xmx2560m", "/usr/share/tink-backend-aggregation/aggregation-service.jar", "server", "/etc/tink/aggregation-server.yml"],
    workdir = "/usr/share/tink-backend-aggregation"
)

container_image(
    name = "provider_configuration_image",
    base = "@openjdk-jdk8//image",
    tars = ["//deb:provider_configuration_tar"],
    cmd = ["java", "-jar", "-Xmx2560m", "/usr/share/tink-backend-provider-configuration/provider-configuration-service.jar", "server", "/etc/tink/provider-configuration-server.yml"],
    workdir = "/usr/share/tink-backend-provider-configuration"
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
    }
)
