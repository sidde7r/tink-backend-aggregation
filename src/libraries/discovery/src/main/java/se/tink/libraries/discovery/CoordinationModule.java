package se.tink.libraries.discovery;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class CoordinationModule extends AbstractModule {
    @Override
    protected void configure() {
        // Deliberately left empty because of the @Provides methods below.
    }

    @Provides
    @Singleton
    public CuratorFramework buildAndStartCuratorFramework(CoordinationConfiguration configuration) {
        RetryPolicy retryPolicy =
                new ExponentialBackoffRetry(
                        configuration.getBaseSleepTimeMs(), configuration.getMaxRetries());
        CuratorFramework client =
                CuratorFrameworkFactory.newClient(configuration.getHosts(), retryPolicy);
        client.start();
        return client;
    }
}
