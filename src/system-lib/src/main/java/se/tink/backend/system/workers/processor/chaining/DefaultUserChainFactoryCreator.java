package se.tink.backend.system.workers.processor.chaining;

import com.google.inject.Inject;
import java.io.IOException;
import se.tink.libraries.cluster.Cluster;

// TODO: Migrate this to a Guice module.
public class DefaultUserChainFactoryCreator implements UserChainFactoryCreator {

    private final Cluster cluster;
    private final ChainFactory chainFactory;

    @Inject
    private DefaultUserChainFactoryCreator(
            Cluster cluster,
            ChainFactory chainFactory) {
        this.cluster = cluster;
        this.chainFactory = chainFactory;
    }

    @Override
    public ChainFactory build() {
        return chainFactory;
    }

    public void close() throws IOException {
        chainFactory.close();
    }
}
