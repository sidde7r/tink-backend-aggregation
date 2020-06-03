# DEPRECATED: Please consider switching to junit_strict.bzl or similar instead.
# https://github.com/tink-ab/tink-backend/pull/17171

SUITE_PACKAGE = "se.tink.backend"
SUITE_SOURCE = """
package %s;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;
import org.junit.BeforeClass;
import io.dropwizard.logging.LoggingFactory;
import ch.qos.logback.classic.Level;

@RunWith(ClasspathSuite.class)
@ClasspathSuite.IncludeJars(true)
@ClasspathSuite.ClassnameFilters("se.tink.*")
public class %s {
    @BeforeClass
    public static void setLogLevelForTests() {
        // This overrides the bootstrapping done from
        // io.dropwizard.Application which is (almost)
        // always on the classpath.
        LoggingFactory.bootstrap(Level.INFO);
    }
}
"""

def generate_suite_implementation(ctx):
    ctx.actions.write(output = ctx.outputs.out, content = SUITE_SOURCE %
                                                          (SUITE_PACKAGE, ctx.attr.outname))

GenerateSuite = rule(
    attrs = {
        "outname": attr.string(),
    },
    outputs = {"out": "%{name}.java"},
    implementation = generate_suite_implementation,
)

def junit_test(name, srcs, deps, **kwargs):
    s_name = name.replace("-", "_") + "TestSuite"
    GenerateSuite(
        name = s_name,
        outname = s_name,
    )
    native.java_test(
        name = name,
        test_class = SUITE_PACKAGE + "." + s_name,
        srcs = srcs + [":" + s_name],
        deps = depset(deps + [
            "//external:cpsuite",
            "//third_party:ch_qos_logback_logback_classic",
            "//third_party:io_dropwizard_dropwizard_logging",
        ]).to_list(),
        **kwargs
    )
