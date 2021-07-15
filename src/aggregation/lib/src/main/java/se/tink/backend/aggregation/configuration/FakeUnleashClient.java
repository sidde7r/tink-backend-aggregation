package se.tink.backend.aggregation.configuration;

import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

public class FakeUnleashClient extends UnleashClient {

    public FakeUnleashClient() {
        super(null);
    }

    @Override
    public void doStop() {}

    @Override
    public void start() {}

    @Override
    public boolean isToggleEnable(Toggle toggle) {
        return true;
    }
}
