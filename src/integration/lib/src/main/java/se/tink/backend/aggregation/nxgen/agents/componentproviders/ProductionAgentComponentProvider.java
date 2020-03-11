package se.tink.backend.aggregation.nxgen.agents.componentproviders;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.NextGenTinkHttpClientProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * @deprecated Agent should implement AgentComponentProvider constructor instead of creating the
 *     providers internally.
 */
@Deprecated
public final class ProductionAgentComponentProvider {

    public static AgentComponentProvider create(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {

        return new AgentComponentProvider(
                new NextGenTinkHttpClientProvider(request, context, signatureKeyPair),
                new SupplementalInformationProviderImpl(context, request),
                new AgentContextProviderImpl(request, context),
                new GeneratedValueProviderImpl(
                        new ActualLocalDateTimeSource(), new RandomValueGeneratorImpl()));
    }
}
