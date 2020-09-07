package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.integration.module;

import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.FakeImageRecognizerHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.ImageRecognizeHelper;

public final class CeWireMockTestModule extends TestModule {

    @Override
    protected void configure() {
        bind(ImageRecognizeHelper.class).to(FakeImageRecognizerHelper.class).in(Scopes.SINGLETON);
    }
}
