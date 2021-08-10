load("@tink_backend//tools/bzl:junit_strict.bzl", junit_test_backend = "junit_test")

def junit_test(**kwargs):
    _set_default_log_level(kwargs)

    junit_test_backend(
        **kwargs
    )

def _set_default_log_level(kwargs):
    # Set a modest default log level in all junit_tests rather than having DEBUG everywhere
    if not "jvm_flags" in kwargs:
        kwargs["jvm_flags"] = [
            "-Dlogback.configurationFile=etc/logback-test.xml",
        ]
    logging_resource = "//etc:logback_test"

    if "resources" in kwargs:
        if logging_resource not in kwargs["resources"]:
            kwargs["resources"].append(logging_resource)
    else:
        kwargs["resources"] = [logging_resource]
