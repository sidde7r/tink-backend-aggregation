load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "sms-gateway-lib",
    srcs = glob(["src/main/**/*.java"]),
    deps = [
        "//src/libraries/log",
        "//src/libraries/net",
        "//src/libraries/serialization_utils:serialization-utils",
        "//src/libraries/uuid:uuid",
        "//src/libraries/metrics",
        
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_client",  
        "//third_party:io_swagger_swagger_annotations",
        "//third_party:org_apache_httpcomponents_httpclient",
    ],
    visibility = ["//visibility:public"],
)

junit_test(
    name = "sms-gateway-lib-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":sms-gateway-lib",
      
        "//third_party:com_google_guava_guava",
        "//third_party:org_assertj_assertj_core",    
    ],
)
