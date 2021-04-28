package se.tink.backend.aggregation.agents.nxgen.demo.banks.bankid;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAgent;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoCreditCardAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoIdentityData;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoInvestmentAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoLoanAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.NameElement;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    IDENTITY_DATA,
    LOANS
})
public final class FailingBankIdDemoAgent extends NextGenerationDemoAgent {

    private DemoBankIdAuthenticator authenticator;

    public FailingBankIdDemoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.authenticator = new DemoBankIdAuthenticator(credentials, false);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(
                new BankIdAuthenticationController<>(
                        supplementalInformationController,
                        authenticator,
                        persistentStorage,
                        credentials,
                        request.getUserAvailability()),
                new PasswordAuthenticationController(authenticator));
    }

    @Override
    public DemoInvestmentAccount getInvestmentAccounts() {
        return new DemoInvestmentAccount() {
            @Override
            public String getAccountId() {
                return "9999-444444444444";
            }

            @Override
            public String getName() {
                return "Investment";
            }

            @Override
            public double getAccountBalance() {
                return 123456;
            }
        };
    }

    @Override
    public DemoSavingsAccount getDemoSavingsAccounts() {
        return new DemoSavingsAccount() {
            @Override
            public String getAccountId() {
                return "9999-222222222222";
            }

            @Override
            public String getAccountName() {
                return "Savings Account";
            }

            @Override
            public double getAccountBalance() {
                return 385245.33;
            }

            @Override
            public List<AccountIdentifier> getIdentifiers() {
                return Collections.emptyList();
            }
        };
    }

    @Override
    public DemoLoanAccount getDemoLoanAccounts() {
        return new DemoLoanAccount() {
            @Override
            public String getMortgageId() {
                return "9999-333333333333";
            }

            @Override
            public String getBlancoId() {
                return "9999-333334444444";
            }

            @Override
            public String getMortgageLoanName() {
                return "Bolån";
            }

            @Override
            public String getBlancoLoanName() {
                return "Santander";
            }

            @Override
            public double getMortgageInterestName() {
                return 0.019;
            }

            @Override
            public double getBlancoInterestName() {
                return 0.145;
            }

            @Override
            public double getMortgageBalance() {
                return -2300000D;
            }

            @Override
            public double getBlancoBalance() {
                return -50000D;
            }

            @Override
            public LocalDate getInitialDate() {
                return null;
            }
        };
    }

    @Override
    public List<DemoTransactionAccount> getTransactionAccounts() {
        return Collections.singletonList(
                new DemoTransactionAccount() {
                    @Override
                    public String getAccountId() {
                        return "9999-111111111111";
                    }

                    @Override
                    public String getAccountName() {
                        return "Debt Account";
                    }

                    @Override
                    public double getBalance() {
                        return 26245.33;
                    }

                    @Override
                    public List<AccountIdentifier> getIdentifiers() {
                        return Collections.emptyList();
                    }
                });
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
                return new ArrayList<>(
                        Arrays.asList(
                                new NameElement(NameElement.Type.FIRST_NAME, "Jane"),
                                new NameElement(NameElement.Type.SURNAME, "Doe")));
            }

            @Override
            public LocalDate getDateOfBirth() {
                return LocalDate.of(1970, 1, 1);
            }
        };
    }
}
