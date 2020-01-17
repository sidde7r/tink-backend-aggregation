package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.authenticator.RedirectOAuth2Authenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.transfer.RedirectDemoTransferExecutor;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.NameElement;

public class RedirectAuthenticationDemoAgent extends NextGenerationDemoAgent
        implements RefreshTransferDestinationExecutor {
    private static final String USERNAME = "tink";

    private String provider;
    private boolean redirectToOxfordStaging;

    public RedirectAuthenticationDemoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.provider = request.getProvider().getName();
        this.redirectToOxfordStaging = Objects.equals("oxford-staging", context.getClusterId());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        // Note: It's on purpose that this agent does not use the
        // AgentConfigurationController to get the callbackUri/redirectUri.
        // This is only for customers to test the callbackUri without
        // having to configure it etc.
        String callbackUri = request.getCallbackUri();

        RedirectOAuth2Authenticator redirectOAuth2Authenticator =
                new RedirectOAuth2Authenticator(redirectToOxfordStaging, callbackUri);

        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        redirectOAuth2Authenticator,
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
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
        String callbackUri = request.getCallbackUri();

        RedirectOAuth2Authenticator redirectOAuth2Authenticator =
                new RedirectOAuth2Authenticator(redirectToOxfordStaging, callbackUri);

        OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        redirectOAuth2Authenticator,
                        credentials,
                        strongAuthenticationState);

        ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController =
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper);

        RedirectDemoTransferExecutor transferExecutor =
                new RedirectDemoTransferExecutor(
                        credentials,
                        supplementalRequester,
                        controller,
                        supplementalInformationHelper,
                        thirdPartyAppAuthenticationController);

        return Optional.of(new TransferController(null, transferExecutor, null, null));
    }

    @Override
    public DemoInvestmentAccount getInvestmentAccounts() {
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
        return DemoAccountDefinitionGenerator.getDemoSavingsAccounts(USERNAME, this.provider);
    }

    @Override
    public DemoLoanAccount getDemoLoanAccounts() {
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
    public List<DemoTransactionAccount> getTransactionAccounts() {
        return Collections.singletonList(
                DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                        USERNAME, this.provider));
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

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        Map<Account, List<TransferDestinationPattern>> transferDestinations = new HashMap<>();
        for (Account account : accounts) {
            if (account.getType() == AccountTypes.CHECKING
                    || account.getType() == AccountTypes.SAVINGS) {
                List<TransferDestinationPattern> destinations = new ArrayList<>();
                destinations.add(
                        TransferDestinationPattern.createForMultiMatch(
                                AccountIdentifier.Type.SE, TransferDestinationPattern.ALL));
                destinations.add(
                        TransferDestinationPattern.createForMultiMatch(
                                AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL));
                destinations.add(
                        TransferDestinationPattern.createForMultiMatch(
                                AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL));
                transferDestinations.put(account, destinations);
            }
        }

        return new FetchTransferDestinationsResponse(transferDestinations);
    }
}
