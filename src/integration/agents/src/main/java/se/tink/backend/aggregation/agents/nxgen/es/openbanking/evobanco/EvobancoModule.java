package se.tink.backend.aggregation.agents.nxgen.es.openbanking.evobanco;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.RedsysGlobalConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class EvobancoModule extends AbstractModule {

    @Provides
    @Singleton
    @Inject
    public ConsentGenerator<ConsentRequestBody> createConsentGenerator(
            AgentComponentProvider agentComponentProvider) {
        return RedsysGlobalConsentGenerator.ofBankDocumentationCombined(
                agentComponentProvider, RedsysConstants.REDSYS_CONSENT_SCOPES);
    }
}
