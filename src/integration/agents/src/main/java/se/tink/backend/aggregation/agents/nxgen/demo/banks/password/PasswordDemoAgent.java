package se.tink.backend.aggregation.agents.nxgen.demo.banks.password;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.password.authenticator.PasswordAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.password.authenticator.PasswordAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoAccountDefinitionGenerator;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAgent;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoCreditCardAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoInvestmentAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoLoanAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class PasswordDemoAgent extends NextGenerationDemoAgent {

    private static String username;
    private static String provider;

    public PasswordDemoAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.username = request.getCredentials().getField("username");
        this.provider = request.getProvider().getName();
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {

    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new PasswordAuthenticator(),
                new PasswordAutoAuthenticator()
        );
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
        return Optional.empty();
    }

    @Override
    public DemoInvestmentAccount getInvestmentAccounts() {
        if (!username.equals("tink")) {
            return null;
        }

        return new DemoInvestmentAccount() {
            @Override
            public String getAccountId() {
                return "7777-444444444444";
            }

            @Override
            public String getName() {
                return "SmallInvestment";
            }

            @Override
            public double getAccountBalance() {
                return 4563;
            }
        };
    }

    @Override
    public DemoSavingsAccount getDemoSavingsAccounts() {
        return DemoAccountDefinitionGenerator.getDemoSavingsAccounts(this.username, this.provider);
    }

    @Override
    public DemoLoanAccount getDemoLoanAccounts() {
        if (!username.equals("tink")) {
            return null;
        }

        return new DemoLoanAccount() {
            @Override
            public String getMortgageId() {
                return "7777-333333333333";
            }

            @Override
            public String getBlancoId() {
                return "7777-333334444444";
            }

            @Override
            public String getMortgageLoanName() {
                return "Loan";
            }

            @Override
            public String getBlancoLoanName() {
                return "Santander";
            }

            @Override
            public double getMortgageInterestName() {
                return 0.53;
            }

            @Override
            public double getBlancoInterestName() {
                return 1.73;
            }

            @Override
            public double getMortgageBalance() {
                return -2300D;
            }

            @Override
            public double getBlancoBalance() {
                return -5D;
            }
        };
    }

    @Override
    public DemoTransactionAccount getTransactionalAccountAccounts() {
        return DemoAccountDefinitionGenerator.getDemoTransactionalAccount(this.username, this.provider);
    }

    @Override
    public List<DemoCreditCardAccount> getCreditCardAccounts() {
        return Collections.emptyList();
    }
}
