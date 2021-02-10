package se.tink.backend.aggregation.agents.nxgen.demo.banks.password;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.common.collect.Lists;
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
import se.tink.backend.aggregation.agents.nxgen.demo.banks.password.authenticator.PasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.password.authenticator.PasswordAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.password.executor.transfer.PasswordDemoPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.password.executor.transfer.PasswordDemoTransferExecutor;
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
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.NameElement;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    LOANS,
    CREDIT_CARDS,
    SAVINGS_ACCOUNTS,
    IDENTITY_DATA,
    TRANSFERS,
    INVESTMENTS,
    MORTGAGE_AGGREGATION
})
public final class PasswordDemoAgent extends NextGenerationDemoAgent
        implements RefreshTransferDestinationExecutor {
    private final String username;
    private final String provider;

    public PasswordDemoAgent(
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
                new PasswordAuthenticator(),
                new PasswordAutoAuthenticator());
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
        PasswordDemoPaymentExecutor paymentExecutor = new PasswordDemoPaymentExecutor();

        return Optional.of(new TransferController(paymentExecutor, transferExecutor));
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
                return 0.053;
            }

            @Override
            public double getBlancoInterestName() {
                return 0.173;
            }

            @Override
            public double getMortgageBalance() {
                return -2300D;
            }

            @Override
            public double getBlancoBalance() {
                return -5D;
            }

            @Override
            public LocalDate getInitialDate() {
                return LocalDate.of(1971, 01, 01);
            }
        };
    }

    @Override
    public List<DemoTransactionAccount> getTransactionAccounts() {
        List<DemoTransactionAccount> accounts = Lists.newArrayList();

        accounts.add(
                DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                        this.username, this.provider));

        accounts.add(
                DemoAccountDefinitionGenerator.getDemoTransactionalAccountWithZeroBalance(
                        this.username, this.provider));

        return accounts;
    }

    @Override
    public List<DemoCreditCardAccount> getCreditCardAccounts() {
        return Collections.singletonList(
                new DemoCreditCardAccount() {
                    @Override
                    public String getAccountId() {
                        return "1122 3344 - 1234";
                    }

                    @Override
                    public String getCreditCardNumber() {
                        return "1234 5678 9101 1121";
                    }

                    @Override
                    public HolderName getNameOnCreditCard() {
                        return new HolderName("Tink Tinkerton");
                    }

                    @Override
                    public String getAccountName() {
                        return "Basic Credit Card";
                    }

                    @Override
                    public double getBalance() {
                        return -1456D;
                    }

                    @Override
                    public double getAvailableCredit() {
                        return 8543D;
                    }
                });
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
