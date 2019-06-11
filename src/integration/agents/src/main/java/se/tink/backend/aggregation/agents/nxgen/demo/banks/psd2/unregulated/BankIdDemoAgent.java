package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.unregulated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.unregulated.authenticator.DemoBankIdAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
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
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.NameElement;

public class BankIdDemoAgent extends NextGenerationDemoAgent {
    private final DemoBankIdAuthenticator authenticator;

    public BankIdDemoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        Provider provider = request.getProvider();

        String name = provider.getName();
        boolean successfulAuthentication = true;
        if (name.contains("failing")) {
            successfulAuthentication = false;
        }

        String[] splitProviderName = name.split("-");
        String lastPart = splitProviderName[splitProviderName.length - 1];

        DemoBankIdAuthenticator.FailCauses failCause = null;
        switch (lastPart) {
            case "timeout":
                failCause = DemoBankIdAuthenticator.FailCauses.TIMEOUT;
                break;
            case "inprogress":
                failCause = DemoBankIdAuthenticator.FailCauses.IN_PROGRESS;
                break;
            case "cancelled":
                failCause = DemoBankIdAuthenticator.FailCauses.CANCELLED;
                break;
            default:
                // do nothing
        }

        this.authenticator = new DemoBankIdAuthenticator(successfulAuthentication, failCause);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(
                new BankIdAuthenticationController<>(supplementalRequester, authenticator));
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
                return null;
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
                return 0.19;
            }

            @Override
            public double getBlancoInterestName() {
                return 1.45;
            }

            @Override
            public double getMortgageBalance() {
                return -2300000D;
            }

            @Override
            public double getBlancoBalance() {
                return -50000D;
            }
        };
    }

    @Override
    public List<DemoTransactionAccount> getTransactionAccounts() {
        return null;
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
