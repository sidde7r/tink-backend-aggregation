package se.tink.backend.aggregation;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;

public final class ClassPathTest {

    @Test
    @Ignore
    public void noConflictingVersionsOfGuava() {
        final String guava = "guava-\\d+\\.\\d+(-jre)?\\.jar";

        URL[] urls = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
        Set<String> guavaJars =
                Stream.of(urls)
                        .map(URL::toString)
                        .map(s -> s.split("/"))
                        .map(tokens -> tokens[tokens.length - 1])
                        .filter(s -> s.matches(guava))
                        .collect(Collectors.toSet());

        assert guavaJars.size() <= 1 : String.format("Conflicting dependencies: %s", guavaJars);
    }

    /*
    Guards against the following exception on aggregation service startup:
    <pre>
    Exception in thread "main" com.google.inject.CreationException: Unable to create injector, see the following errors:
    1) No implementation for se.tink.backend.aggregation.storage.database.repositories.AggregatorConfigurationsRepository was bound.
    [...]
    at se.tink.libraries.repository.config.repository.AggregationRepositoryConfiguration$$EnhancerBySpringCGLIB$$a9e6da6e.entityManagerFactory(<generated>)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:498)
    at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:162)
    ... 33 more
    Caused by: java.lang.ClassNotFoundException: org.hibernate.ejb.HibernateEntityManagerFactory
    at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
    at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
    at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:352)
    at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
    at org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter.<init>(HibernateJpaVendorAdapter.java:89)
    ... 48 more
    </pre>
     */
    @Test
    public void hibernateEntityManagerFactoryShouldBePresent() throws ClassNotFoundException {
        Class.forName("org.hibernate.ejb.HibernateEntityManagerFactory");
    }
}
