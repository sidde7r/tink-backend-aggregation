package se.tink.backend.aggregation.agents.nxgen.demo.banks.multisupplemental;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.multisupplemental.authenticator.MultiSupplementalAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.multisupplemental.authenticator.MultiSupplementalManualAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAgent;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoInvestmentAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoLoanAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoTransactionAccount;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class MultiSupplementalDemoAgent extends NextGenerationDemoAgent {

    public MultiSupplementalDemoAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {

    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                context,
                new MultiSupplementalManualAuthenticator(supplementalInformationController, catalog),
                new MultiSupplementalAutoAuthenticator()
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
    public DemoInvestmentAccount getInvestmentDefinitions() {
        return new DemoInvestmentAccount() {
            @Override
            public String getAccountId() {
                return "8888-444444444444";
            }

            @Override
            public String getName() {
                return "Small Investment";
            }

            @Override
            public double getAccountBalance() {
                return 456;
            }
        };
    }

    @Override
    public DemoSavingsAccount getDemoSavingsDefinitions() {
        return new DemoSavingsAccount() {
            @Override
            public String getAccountId() {
                return "8888-222222222222";
            }

            @Override
            public String getAccountName() {
                return "Savings Account";
            }

            @Override
            public double getAccountBalance() {
                return 245.33;
            }
        };
    }

    @Override
    public DemoLoanAccount getDemoLoanDefinitions() {
        return new DemoLoanAccount() {
            @Override
            public String getMortgageId() {
                return "8888-333333333333";
            }

            @Override
            public String getBlancoId() {
                return "8888-333334444444";
            }

            @Override
            public String getMortgageLoanName() {
                return "Loan";
            }

            @Override
            public String getBlancoLoanName() {
                return "Tesco Invest";
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
    public DemoTransactionAccount getTransactionalAccountDefinition() {
        return new DemoTransactionAccount() {
            @Override
            public String getAccountId() {
                return "8888-111111111111";
            }

            @Override
            public String getAccountName() {
                return "Checking Account";
            }

            @Override
            public double getBalance() {
                return  25.33;
            }
        };
    }
}
