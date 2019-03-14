load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_image",
    "container_push",
)

container_image(
    name = "docker",
    base = "@python-3_6-alpine//image",
    cmd = [
        "pip3 install -r requirements.txt"
    ],
    files = [
        "requirements.txt",
        "cronjob.py",
    ],
    entrypoint = [
        "/cronjob.py"
    ],
    tags = ["manual"],
)

container_push(
    name = "docker_push",
    format = "Docker",
    image = ":docker",
    registry = "gcr.io",
    repository = "tink-containers/tink-backend-aggregation-statuspage-providers-cronjob",
    tag = "{TINK_VERSION}",
    tags = ["manual"],
)
