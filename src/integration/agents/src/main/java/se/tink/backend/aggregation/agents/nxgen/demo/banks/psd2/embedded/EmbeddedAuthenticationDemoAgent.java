package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.embedded;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.password.executor.transfer.PasswordDemoTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.embedded.authenticator.EmbeddedAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.embedded.authenticator.EmbeddedAutoAuthenticator;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoAccountDefinitionGenerator;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAgent;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoCreditCardAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoIdentityData;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoInvestmentAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoLoanAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.NameElement;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    IDENTITY_DATA,
    LOANS,
    MORTGAGE_AGGREGATION
})
public final class EmbeddedAuthenticationDemoAgent extends NextGenerationDemoAgent
        implements RefreshTransferDestinationExecutor {
    private static final int DAYS_UNTIL_SESSION_SHOULD_EXPIRE = 90;
    private final String username;
    private final String provider;

    public EmbeddedAuthenticationDemoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.username = request.getCredentials().getField(Field.Key.USERNAME);
        this.provider = request.getProvider().getName();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new EmbeddedAuthenticator(DAYS_UNTIL_SESSION_SHOULD_EXPIRE),
                new EmbeddedAutoAuthenticator());
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
                new PasswordDemoTransferExecutor(credentials);

        return Optional.of(new TransferController(null, transferExecutor));
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
    public List<DemoTransactionAccount> getTransactionAccounts() {
        return Collections.singletonList(
                DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                        this.username, this.provider));
    }

    @Override
    public List<DemoCreditCardAccount> getCreditCardAccounts() {
        return Collections.emptyList();
    }

    @Override
    public DemoIdentityData getIdentityDataResponse() {
        return new DemoIdentityData() {
            @Override
            public List<NameElement> getNameElements() {
                switch (username) {
                    case "tink2":
                        return new ArrayList<>(
                                Arrays.asList(
                                        new NameElement(NameElement.Type.FIRST_NAME, "John"),
                                        new NameElement(NameElement.Type.SURNAME, "Doe")));
                    case "tink3":
                        return new ArrayList<>(
                                Arrays.asList(
                                        new NameElement(NameElement.Type.FIRST_NAME, "Mary"),
                                        new NameElement(NameElement.Type.SURNAME, "Sue")));
                    default:
                        return new ArrayList<>(
                                Arrays.asList(
                                        new NameElement(NameElement.Type.FIRST_NAME, "Jane"),
                                        new NameElement(NameElement.Type.SURNAME, "Doe")));
                }
            }

            @Override
            public LocalDate getDateOfBirth() {
                return LocalDate.of(1970, 1, 1);
            }
        };
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return new FetchTransferDestinationsResponse(
                DemoAccountDefinitionGenerator.generateTransferDestinations(accounts));
    }
}
