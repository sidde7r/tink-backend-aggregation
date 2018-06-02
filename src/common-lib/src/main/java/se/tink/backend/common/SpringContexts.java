package se.tink.backend.common;

import com.google.inject.Inject;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.tink.backend.guice.annotations.Centralized;
import se.tink.backend.guice.annotations.Distributed;

public class SpringContexts {
    private final AnnotationConfigApplicationContext centralizedContext;
    private final AnnotationConfigApplicationContext distributedContext;

    // Beware that injection may fail if either of DatabaseConfiguration
    // or DistributedDatabaseConfiguration are disabled for a command
    @Inject
    public SpringContexts(
            @Centralized AnnotationConfigApplicationContext centralizedContext,
            @Distributed AnnotationConfigApplicationContext distributedContext
    )  {
        this.centralizedContext = centralizedContext;
        this.distributedContext = distributedContext;
    }

     public void close() {
        centralizedContext.close();
        distributedContext.close();
    }

}
