package se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbanken.bankid;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.CrossKeyBankIdAuthenticator;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    IDENTITY_DATA,
    LOANS
})
public final class AlandsBankenBankidSeAgent extends CrossKeyAgent {

    public AlandsBankenBankidSeAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AlandsBankenBankidSeConfiguration());
    }

    @Override
    protected Authenticator constructAuthenticator() {

        return new BankIdAuthenticationController<>(
                supplementalInformationController,
                new CrossKeyBankIdAuthenticator(
                        apiClient, agentConfiguration, sessionStorage, credentials),
                persistentStorage,
                credentials);
    }
}
