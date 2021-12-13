package se.tink.backend.aggregation.configuration;

import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

// TODO: Move to our Unleash wrapping lib in tick-backend
public class FakeUnleashClient extends UnleashClient {

    public FakeUnleashClient() {
        super(null);
    }

    @Override
    public void doStop() {}

    @Override
    public void start() {}

    @Override
    public boolean isToggleEnabled(Toggle toggle) {
        return true;
    }
}
