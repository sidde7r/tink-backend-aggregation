package se.tink.backend.aggregation.agents.nxgen.fi.banks.poppankki;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkV2Configuration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class PopPankkiAgent extends SamlinkAgent {

    public PopPankkiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                new SamlinkV2Configuration(
                        PopPankkiConstants.Url.BASE, PopPankkiConstants.Header.CLIENT_APP_VALUE));
    }
}
