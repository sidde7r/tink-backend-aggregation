package se.tink.backend.aggregation.provider.configuration.cli;

import com.google.inject.Inject;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.tink.libraries.repository.guice.annotations.Centralized;

public class ProviderConfigurationSpringContext {
    private final AnnotationConfigApplicationContext centralizedContext;

    @Inject
    public ProviderConfigurationSpringContext(
            @Centralized AnnotationConfigApplicationContext centralizedContext) {
        this.centralizedContext = centralizedContext;
    }

    public void close() {
        centralizedContext.close();
    }
}
