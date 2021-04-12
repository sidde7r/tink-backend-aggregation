package se.tink.backend.aggregation.nxgen.agents.componentproviders;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.NextGenTinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.http.event.FakeNextGenTinkHttpClientEventProducer;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * @deprecated Agent should implement AgentComponentProvider constructor instead of creating the
 *     providers internally.
 */
@Deprecated
public final class ProductionAgentComponentProvider {

    @Deprecated
    public static AgentComponentProvider create(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {

        /*
           We are not planning to support raw bank data event emission
           for agents that use deprecated ProductionAgentComponentProvider (at least initially).
           This will be a good incentive to migrate them to use AgentComponentProvider constructor
        */
        return new AgentComponentProvider(
                new NextGenTinkHttpClientProvider(
                        request,
                        context,
                        signatureKeyPair,
                        new FakeNextGenTinkHttpClientEventProducer()),
                new SupplementalInformationProviderImpl(context, request),
                new AgentContextProviderImpl(request, context),
                new GeneratedValueProviderImpl(
                        new ActualLocalDateTimeSource(), new RandomValueGeneratorImpl()));
    }
}
