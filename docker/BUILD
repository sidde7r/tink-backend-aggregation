load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_image",
    "container_bundle",
    container_repositories = "repositories",
)

container_image(
    name = "aggregation_image",
    base = "@openjdk-jdk8//image",
    tars = ["//deb:aggregation_tar"],
    cmd = ["java", "-jar", "-Xmx2560m", "/usr/share/tink-backend-aggregation/aggregation-service.jar", "server", "/etc/tink/aggregation-server.yml"],
    workdir = "/usr/share/tink-backend-aggregation"
)

container_image(
    name = "connector_image",
    base = "@openjdk-jdk8//image",
    tars = ["//deb:connector_tar"],
    cmd = ["java", "-jar", "-Xmx2560m", "/usr/share/tink-backend-connector/connector-service.jar", "server", "/etc/tink/connector-server.yml"],
    workdir = "/usr/share/tink-backend-connector"
)

container_image(
    name = "main_image",
    base = "@openjdk-jdk8//image",
    tars = ["//deb:main_tar"],
    cmd = ["java", "-jar", "-Xmx2560m", "/usr/share/tink-backend-main/main-service.jar", "server", "/etc/tink/main-server.yml"],
    workdir = "/usr/share/tink-backend-main"
)

container_image(
    name = "system_image",
    base = "@openjdk-jdk8//image",
    tars = ["//deb:system_tar"],
    cmd = ["java", "-jar", "-Xmx2560m", "/usr/share/tink-backend-system/system-service.jar", "server", "/etc/tink/system-server.yml"],
    workdir = "/usr/share/tink-backend-system"
)

container_image(
    name = "webhook_image",
    base = "@openjdk-jdk8//image",
    tars = ["//deb:webhook_tar"],
    cmd = ["java", "-jar", "-Xmx256m", "/usr/share/tink-backend-webhook/webhook-service.jar", "server", "/etc/tink/webhook-server.yml"],
    workdir = "/usr/share/tink-backend-webhook"
)

container_image(
    name = "insights_image",
    base = "@openjdk-jdk8//image",
    cmd = ["java", "-jar", "-Xmx256m", "/usr/share/tink-backend-insights/insights-service.jar", "server", "/etc/tink/insights-server.yml"],
    tars = ["//deb:insights_tar"],
    workdir = "/usr/share/tink-backend-insights",
)

container_image(
    name = "data_export_image",
    base = "@openjdk-jdk8//image",
    cmd = ["java", "-jar", "-Xmx256m", "/usr/share/tink-backend-data-export/data-export-service.jar", "server", "/etc/tink/data-export-server.yml"],
    tars = ["//deb:data_export_tar"],
    workdir = "/usr/share/tink-backend-data-export",
)

container_image(
    name = "product_executor_image",
    base = "@openjdk-jdk8//image",
    cmd = ["java", "-jar", "-Xmx256m", "/usr/share/tink-backend-product-executor/product-executor-service.jar", "server", "/etc/tink/product-executor-server.yml"],
    tars = ["//deb:product_executor_tar"],
    workdir = "/usr/share/tink-backend-product-executor",
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
        "gcr.io/tink-containers/tink-backend-connector:latest": ":connector_image",
        "gcr.io/tink-containers/tink-backend-main:latest": ":main_image",
        "gcr.io/tink-containers/tink-backend-system:latest": ":system_image",
        "gcr.io/tink-containers/tink-backend-webhook:latest": ":webhook_image",
        "gcr.io/tink-containers/tink-backend-insights:latest": "insights_image",
        "gcr.io/tink-containers/tink-backend-data-export:latest": "data_export_image",
        "gcr.io/tink-containers/tink-backend-product-executor:latest": "product_executor_image",
    }
)
