package se.tink.backend.aggregation.agents.nxgen.demo.banks.finovate;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
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
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.NameElement;

/*
This is a temporary solution and should be deleted as soon as the demo is done
 BAWAG: Will have salary and savings
 Erste: Will have loans
 Easy:  Will have investments
 */
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, LOANS})
public final class PasswordDemoAgent extends NextGenerationDemoAgent {
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
    public DemoInvestmentAccount getInvestmentAccounts() {
        if (request.getProvider().getName().equals("at-test-easy-bank")) {

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
                    return 10000;
                }
            };
        }

        return null;
    }

    @Override
    public DemoSavingsAccount getDemoSavingsAccounts() {
        if (request.getProvider().getName().equals("at-test-erste-bank")) {
            return DemoAccountDefinitionGenerator.getDemoSavingsAccounts(
                    this.username, this.provider);
        }

        return null;
    }

    @Override
    public DemoLoanAccount getDemoLoanAccounts() {
        if (request.getProvider().getName().equals("at-test-bawag")) {

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
                    return null;
                }
            };
        }

        return null;
    }

    @Override
    public List<DemoTransactionAccount> getTransactionAccounts() {
        if (request.getProvider().getName().equals("at-test-erste-bank")) {
            return Collections.singletonList(
                    DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                            this.username, this.provider));
        }

        return Collections.emptyList();
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
