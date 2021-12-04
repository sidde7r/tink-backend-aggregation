load("@rules_jvm_external//:defs.bzl", "maven_install")

GUAVA_VERSION = "30.1.1-jre"

SLF4J_VERSION = "1.7.32"
LOGBACK_VERSION = "1.2.6"
JACKSON_VERSION = "2.13.0"
BOUNCYCASTLE_VERSION = "1.69"

# After updating this file you must run:
# $ bazel run @unpinned_agent_sdk_maven//:pin
def agent_sdk_deps():
    maven_install(
        name = "agent_sdk_maven",
        artifacts = [
            "javax.inject:javax.inject:1",
            "com.google.code.findbugs:jsr305:3.0.2",
            "org.projectlombok:lombok:1.18.22",
            "com.google.guava:guava:%s" % GUAVA_VERSION,
            "com.google.guava:guava-testlib:%s" % GUAVA_VERSION,
            "ch.qos.logback:logback-classic:%s" % LOGBACK_VERSION,
            "org.slf4j:slf4j-api:%s" % SLF4J_VERSION,
            "com.fasterxml.jackson.core:jackson-databind:%s" % JACKSON_VERSION,
            "com.fasterxml.jackson.core:jackson-core:%s" % JACKSON_VERSION,
            "com.fasterxml.jackson.core:jackson-annotations:%s" % JACKSON_VERSION,
            "com.fasterxml.jackson.dataformat:jackson-dataformat-smile:%s" % JACKSON_VERSION,
            "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:%s" % JACKSON_VERSION,
            "com.google.inject:guice:5.0.1",
            "org.bouncycastle:bcpkix-jdk15on:%s" % BOUNCYCASTLE_VERSION,
            "org.bouncycastle:bcprov-jdk15on:%s" % BOUNCYCASTLE_VERSION,
            "commons-codec:commons-codec:1.15",
            "org.apache.commons:commons-lang3:3.12.0",
            "org.apache.httpcomponents:httpclient:4.5.13",
            "org.apache.httpcomponents:httpcore:4.4.14",
            "org.iban4j:iban4j:3.2.3-RELEASE",
            "commons-validator:commons-validator:1.7",
        ],
        fetch_sources = True,
        version_conflict_policy = "pinned",
        maven_install_json = "//src/agent_sdk:agent_sdk_maven_install.json",
        repositories = [
            "https://repo1.maven.org/maven2",
        ],
    )
