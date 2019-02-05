package se.tink.backend.aggregation.cli;

import com.google.inject.Inject;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.tink.libraries.repository.guice.annotations.Centralized;

public class AggregationSpringContext {
    private final AnnotationConfigApplicationContext centralizedContext;

    @Inject
    public AggregationSpringContext(@Centralized AnnotationConfigApplicationContext centralizedContext)  {
        this.centralizedContext = centralizedContext;
    }

    public void close() {
        centralizedContext.close();
    }
}
