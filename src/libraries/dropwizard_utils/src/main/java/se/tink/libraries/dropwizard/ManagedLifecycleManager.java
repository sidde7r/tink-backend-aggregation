package se.tink.libraries.dropwizard;

import com.netflix.governator.lifecycle.LifecycleManager;
import io.dropwizard.lifecycle.Managed;

public class ManagedLifecycleManager implements Managed {
    private final LifecycleManager lifecycleManager;

    ManagedLifecycleManager(LifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }

    @Override
    public void start() throws Exception {
        lifecycleManager.start();
    }

    @Override
    public void stop() throws Exception {
        lifecycleManager.close();
    }
}
