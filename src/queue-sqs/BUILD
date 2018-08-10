java_library(
     name = "queue-sqs",
     srcs = glob(["src/main/**/*.java"]),
     resources = glob(["src/main/resources/**"]),
     visibility = ["//visibility:public"],
     deps = [
         "//src/queue-lib:queue-lib",
         "//third_party:com_google_guava_guava",
         "//third_party:com_amazonaws_aws_java_sdk_sqs",
         "//third_party:com_google_inject_guice",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:io_dropwizard_dropwizard_core",
        "//src/libraries/serialization_utils:serialization-utils",
        "//third_party:org_xerial_snappy_snappy_java",

     ]
 )
