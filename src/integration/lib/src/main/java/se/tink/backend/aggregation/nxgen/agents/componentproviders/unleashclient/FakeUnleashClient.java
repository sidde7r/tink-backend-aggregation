package se.tink.backend.aggregation.nxgen.agents.componentproviders.unleashclient;

import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

// TODO: IFD-3096 Move to Unleash wrapping lib in tick-backend
public class FakeUnleashClient extends UnleashClient {

    private final boolean allTogglesEnabled;

    public FakeUnleashClient(boolean allTogglesEnabled) {
        super(null);
        this.allTogglesEnabled = allTogglesEnabled;
    }

    @Override
    public void doStop() {
        // Do nothing as there is no real Unleash instance running
    }

    @Override
    public void start() {
        // Do nothing as there is no real Unleash instance running
    }

    @Override
    public boolean isToggleEnable(Toggle toggle) {
        return allTogglesEnabled;
    }
}
