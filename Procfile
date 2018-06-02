encryption: bash -c "cd ../tink-backend-encryption && bazel run :encryption --jvmopt=-Djava.net.preferIPv4Stack=true server etc/development-encryption-server.yml"
aggregation: bazel run :aggregation --jvmopt=-Djava.net.preferIPv4Stack=true server etc/development-aggregation-server.yml
system: bazel run :system --jvmopt=-Djava.net.preferIPv4Stack=true server etc/development-system-server.yml
main: bazel run :main --jvmopt=-Djava.net.preferIPv4Stack=true server etc/development-main-server.yml
webhook: bazel run :webhook --jvmopt=-Djava.net.preferIPv4Stack=true server etc/development-webhook-server.yml
aggregation-controller: bazel run :aggregation-controller --jvmopt=-Djava.net.preferIPv4Stack=true server etc/development-aggregation-controller-server.yml
data-export: bazel run :data-export --jvmopt=-Djava.net.preferIPv4Stack=true server etc/development-data-export-server.yml
