package se.tink.libraries.repository.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.tink.libraries.repository.guice.annotations.Centralized;

public class SpringIntegrationProvider<T> implements Provider<T> {
    private static final Logger log = LoggerFactory.getLogger(SpringIntegrationProvider.class);
    private AnnotationConfigApplicationContext applicationContext;
    private AnnotationConfigApplicationContext distributedApplicationContext;
    private final Class<T> cls;

    private SpringIntegrationProvider(Class<T> cls) {
        this.cls = cls;
    }

    @Inject
    void initialize(@Nullable @Centralized AnnotationConfigApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public T get() {
        if (applicationContext == null) {
            log.warn("Centralized repository not initialized.");
            return null;
        }
        return applicationContext.getBean(cls);
    }

    public static <T> SpringIntegrationProvider<T> fromSpring(Class<T> tClass) {
        return new SpringIntegrationProvider<>(tClass);
    }
}
