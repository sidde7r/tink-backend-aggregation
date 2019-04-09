package se.tink.libraries.repository.providers;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.tink.libraries.repository.guice.annotations.Centralized;
import se.tink.libraries.repository.source.RepositorySource;

public class SpringIntegrationProvider<T> implements Provider<T> {
    private static final Logger log = LoggerFactory.getLogger(SpringIntegrationProvider.class);
    private AnnotationConfigApplicationContext applicationContext;
    private AnnotationConfigApplicationContext distributedApplicationContext;
    private final Class<T> cls;
    private final RepositorySource source;

    private SpringIntegrationProvider(Class<T> cls, RepositorySource source) {
        this.cls = cls;
        this.source = source;
    }

    @Inject
    void initialize(@Nullable @Centralized AnnotationConfigApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public T get() {
        return getRepository(cls, source);
    }

    public static <T> SpringIntegrationProvider<T> fromSpring(Class<T> tClass) {
        return new SpringIntegrationProvider<>(tClass, RepositorySource.DEFAULT);
    }

    private <R> R getRepository(Class<R> cls, RepositorySource source) {
        Preconditions.checkNotNull(source);

        switch (source) {
            case CENTRALIZED:
                if (applicationContext == null) {
                    log.warn("Centralized repository not initialized.");
                    return null;
                }
                return applicationContext.getBean(cls);
            default:
                // Default to centralized
                return getRepository(cls, RepositorySource.CENTRALIZED);
        }
    }
}
