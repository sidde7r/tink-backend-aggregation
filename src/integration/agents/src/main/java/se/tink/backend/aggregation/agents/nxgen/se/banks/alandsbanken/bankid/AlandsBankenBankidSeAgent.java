package se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbanken.bankid;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.CrossKeyBankIdAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AlandsBankenBankidSeAgent extends CrossKeyAgent {

    public AlandsBankenBankidSeAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AlandsBankenBankidSeConfiguration());
    }

    @Override
    protected Authenticator constructAuthenticator() {

        return new BankIdAuthenticationController<>(
                supplementalRequester,
                new CrossKeyBankIdAuthenticator(
                        apiClient, agentConfiguration, sessionStorage, credentials));
    }
}
