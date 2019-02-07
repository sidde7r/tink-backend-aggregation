package se.tink.backend.libraries.api.annotations;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import org.junit.Assert;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.log.LogUtils;

public class ApiTeamOwnershipChecker {

    private static final Logger log = LoggerFactory.getLogger(ApiTeamOwnershipChecker.class);
    private static final String DEFAULT_PACKAGE_PREFIX = "se.tink";

    private final String packagePrefix;

    public ApiTeamOwnershipChecker(String packagePrefix) {
        this.packagePrefix = packagePrefix;
    }

    public void check() {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packagePrefix)))
                        .setUrls(ClasspathHelper.forPackage("se.tink"))
                        .setScanners(new MethodAnnotationsScanner()));

        Set<Method> pathMethods = ImmutableSet.<Method>builder()
                .addAll(reflections.getMethodsAnnotatedWith(GET.class))
                .addAll(reflections.getMethodsAnnotatedWith(POST.class))
                .addAll(reflections.getMethodsAnnotatedWith(DELETE.class))
                .addAll(reflections.getMethodsAnnotatedWith(HEAD.class))
                .addAll(reflections.getMethodsAnnotatedWith(OPTIONS.class))
                .addAll(reflections.getMethodsAnnotatedWith(PUT.class))
                .build();

        Assert.assertFalse("Could not find any method annotated with HTTP verb. Please update test.",
                pathMethods.isEmpty());

        Set<Method> unannotatedResources = pathMethods
                .stream()
                .filter(type -> !hasTeamOwnershipAnnotation(type))
                .collect(Collectors.toSet());

        if (!unannotatedResources.isEmpty()) {
            log.error("There were API methods incorrectly annotated:");
            unannotatedResources
                    .forEach(m -> log.error(String.format(" * %s#%s", m.getDeclaringClass().getName(), m.getName())));
            Assert.fail("There were API methods incorrectly annotated. See logged list.");
        }
    }

    // visible for testing
    static boolean hasTeamOwnershipAnnotation(Method type) {
        return type.getDeclaredAnnotation(TeamOwnership.class) != null;
    }

}
