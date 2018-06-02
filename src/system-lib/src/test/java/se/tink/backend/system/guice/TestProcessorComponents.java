package se.tink.backend.system.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.util.Providers;
import se.tink.backend.common.product.targeting.TargetProductsRunnableFactory;
import se.tink.backend.core.User;
import se.tink.backend.system.workers.processor.TransactionProcessor;
import se.tink.backend.util.TestProcessor;
import se.tink.backend.util.TestUtil;
import se.tink.libraries.metrics.MetricRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestProcessorComponents extends AbstractModule {
    @Override
    protected void configure() {
        bind(TestUtil.class);
        bind(MetricRegistry.class).in(Scopes.SINGLETON);
        bind(TransactionProcessor.class);
        bind(TestProcessor.class);
        TargetProductsRunnableFactory targetProductsRunnableFactory = mock(TargetProductsRunnableFactory.class);
        when(targetProductsRunnableFactory.createRunnable(any(User.class))).thenReturn(null);
        bind(TargetProductsRunnableFactory.class).toProvider(Providers.of(targetProductsRunnableFactory));
    }
}
