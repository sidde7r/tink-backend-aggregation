package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.password.executor.transfer.PasswordDemoTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.authenticator.RedirectThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoAccountDefinitionGenerator;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAgent;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoCreditCardAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoIdentityData;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoInvestmentAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoLoanAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class RedirectAuthenticationDemoAgent extends NextGenerationDemoAgent {
    private static String username;
    private static String provider;

    public RedirectAuthenticationDemoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.username = request.getCredentials().getField("username");
        this.provider = request.getProvider().getName();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        RedirectThirdPartyAppAuthenticator redirectThirdPartyAppAuthenticator =
                RedirectThirdPartyAppAuthenticator.createSuccessfulAuthenticator(username);

        return new ThirdPartyAppAuthenticationController<>(
                redirectThirdPartyAppAuthenticator, supplementalInformationHelper);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SessionHandler() {
            @Override
            public void logout() {
                // nop.
            }

            @Override
            public void keepAlive() throws SessionException {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        };
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        PasswordDemoTransferExecutor transferExecutor =
                new PasswordDemoTransferExecutor(credentials, supplementalRequester);

        return Optional.of(new TransferController(null, transferExecutor, null, null));
    }

    @Override
    public DemoInvestmentAccount getInvestmentAccounts() {
        return null;
    }

    @Override
    public DemoSavingsAccount getDemoSavingsAccounts() {
        return null;
    }

    @Override
    public DemoLoanAccount getDemoLoanAccounts() {
        return null;
    }

    @Override
    public DemoTransactionAccount getTransactionalAccountAccounts() {
        return DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                this.username, this.provider);
    }

    @Override
    public List<DemoCreditCardAccount> getCreditCardAccounts() {
        return Collections.emptyList();
    }

    @Override
    public DemoIdentityData getIdentityDataResponse() {
        return null;
    }
}
