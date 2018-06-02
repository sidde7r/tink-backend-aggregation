package se.tink.backend.common.providers;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.annotation.Nullable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.guice.annotations.Centralized;
import se.tink.backend.guice.annotations.Distributed;

public class SpringIntegrationProvider<T> implements Provider<T> {
    private final static LogUtils log = new LogUtils(SpringIntegrationProvider.class);
    private AnnotationConfigApplicationContext applicationContext;
    private AnnotationConfigApplicationContext distributedApplicationContext;
    private final Class<T> cls;
    private final ServiceContext.RepositorySource source;

    private SpringIntegrationProvider(Class<T> cls, ServiceContext.RepositorySource source) {
        this.cls = cls;
        this.source = source;
    }

    @Inject
    void initialize(
            @Nullable @Centralized AnnotationConfigApplicationContext applicationContext,
            @Nullable @Distributed AnnotationConfigApplicationContext distributedApplicationContext) {
        this.applicationContext = applicationContext;
        this.distributedApplicationContext = distributedApplicationContext;
    }

    @Override
    public T get() {
        return getRepository(cls, source);
    }

    public static <T> SpringIntegrationProvider<T> fromSpring(Class<T> tClass) {
        return new SpringIntegrationProvider<>(tClass, ServiceContext.RepositorySource.DEFAULT);
    }

    private <R> R getRepository(Class<R> cls, ServiceContext.RepositorySource source) {
        Preconditions.checkNotNull(source);

        switch (source) {
            case DISTRIBUTED:
                if (distributedApplicationContext == null) {
                    log.warn("Distributed repository not initialized.");
                    return null;
                }
                return distributedApplicationContext.getBean(cls);
            case CENTRALIZED:
                if (applicationContext == null) {
                    log.warn("Centralized repository not initialized.");
                    return null;
                }
                return applicationContext.getBean(cls);
            default:
                if (CassandraRepository.class.isAssignableFrom(cls)) {
                    // There _are_ super interfaces of CassandraRepository, but this should cover 99% of our cases.
                    return getRepository(cls, ServiceContext.RepositorySource.DISTRIBUTED);
                }

                // Default to centralized
                return getRepository(cls, ServiceContext.RepositorySource.CENTRALIZED);
        }
    }
}
