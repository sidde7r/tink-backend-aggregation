package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect;

import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.DEMO_PROVIDER_CONFIGURABLE_SESSION_CASE_REGEX;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.CreditCard;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.IdentityData;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.InvestmentAccounts;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.LoanAccount;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.StaticAccountUK;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.authenticator.RedirectOAuth2Authenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.transfer.RedirectDemoPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.transfer.RedirectDemoTransferExecutor;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoAccountDefinitionGenerator;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
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
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
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
                new RedirectOAuth2Authenticator(redirectToOxfordStaging, callbackUri, credentials);

        final OAuth2AuthenticationController controller;

        if (provider.matches(DEMO_PROVIDER_CONFIGURABLE_SESSION_CASE_REGEX)) {
            controller =
                    new OAuth2AuthenticationController(
                            persistentStorage,
                            supplementalInformationHelper,
                            redirectOAuth2Authenticator,
                            credentials,
                            strongAuthenticationState,
                            Integer.parseInt(credentials.getField(Field.Key.SESSION_EXPIRY_TIME)),
                            ChronoUnit.SECONDS);
        } else {
            controller =
                    new OAuth2AuthenticationController(
                            persistentStorage,
                            supplementalInformationHelper,
                            redirectOAuth2Authenticator,
                            credentials,
                            strongAuthenticationState);
        }

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
                new RedirectOAuth2Authenticator(redirectToOxfordStaging, callbackUri, credentials);

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
    public Optional<PaymentController> constructPaymentController() {
        String callbackUri = request.getCallbackUri();

        RedirectOAuth2Authenticator redirectOAuth2Authenticator =
                new RedirectOAuth2Authenticator(redirectToOxfordStaging, callbackUri, credentials);

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

        RedirectDemoPaymentExecutor paymentExecutor =
                new RedirectDemoPaymentExecutor(
                        credentials,
                        supplementalRequester,
                        controller,
                        supplementalInformationHelper,
                        thirdPartyAppAuthenticationController,
                        strongAuthenticationState);

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    @Override
    public DemoInvestmentAccount getInvestmentAccounts() {
        return new DemoInvestmentAccount() {
            @Override
            public String getAccountId() {
                return InvestmentAccounts.ACCOUNTID;
            }

            @Override
            public String getName() {
                return InvestmentAccounts.ACCOUNTNAME;
            }

            @Override
            public double getAccountBalance() {
                return InvestmentAccounts.ACCOUNTBALANCE;
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
                return LoanAccount.MORTGAGEID;
            }

            @Override
            public String getBlancoId() {
                return LoanAccount.BLANCOID;
            }

            @Override
            public String getMortgageLoanName() {
                return LoanAccount.MORTGAGELOANNAME;
            }

            @Override
            public String getBlancoLoanName() {
                return LoanAccount.BLANCOLOANNAME;
            }

            @Override
            public double getMortgageInterestName() {
                return LoanAccount.MORTGAGEINTERESTNAME;
            }

            @Override
            public double getBlancoInterestName() {
                return LoanAccount.BLANCOINTERESTNAME;
            }

            @Override
            public double getMortgageBalance() {
                return LoanAccount.MORTGAGEBALANCE;
            }

            @Override
            public double getBlancoBalance() {
                return LoanAccount.BLANCOBALANCE;
            }
        };
    }

    @Override
    public List<DemoTransactionAccount> getTransactionAccounts() {
        if (this.provider.matches(DemoConstants.MARKET_REGEX.UK_PROVIDERS_REGEX)) {
            return Lists.newArrayList(
                    DemoAccountDefinitionGenerator.getDemoTransactionalAccountWithZeroBalance(
                            USERNAME, this.provider),
                    DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                            USERNAME, this.provider, 0),
                    DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                            USERNAME, this.provider, 1),
                    new DemoTransactionAccount() {
                        @Override
                        public String getAccountId() {
                            return StaticAccountUK.ACCOUNT_ID;
                        }

                        @Override
                        public String getAccountName() {
                            return StaticAccountUK.ACCOUNT_NAME;
                        }

                        @Override
                        public double getBalance() {
                            return StaticAccountUK.BALANCE;
                        }

                        @Override
                        public Optional<Double> getAvailableBalance() {
                            return Optional.of(StaticAccountUK.AVAILABLE_BALANCE);
                        }

                        @Override
                        public Optional<Double> getCreditLimit() {
                            return Optional.of(StaticAccountUK.CREDIT_LIMIT);
                        }

                        @Override
                        public List<AccountIdentifier> getIdentifiers() {
                            return Lists.newArrayList(
                                    AccountIdentifier.create(
                                            AccountIdentifier.Type.SORT_CODE,
                                            getAccountId(),
                                            StaticAccountUK.ACCOUNT_IDENTIFIERS));
                        }
                    });

        } else {
            return Lists.newArrayList(
                    DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                            USERNAME, this.provider));
        }
    }

    @Override
    public List<DemoCreditCardAccount> getCreditCardAccounts() {
        return Collections.singletonList(
                new DemoCreditCardAccount() {
                    @Override
                    public String getAccountId() {
                        return CreditCard.ACCOUNTID;
                    }

                    @Override
                    public String getCreditCardNumber() {
                        return CreditCard.CREDITCARDNUMBER;
                    }

                    @Override
                    public HolderName getNameOnCreditCard() {
                        return new HolderName(CreditCard.NAMEONCREDITCARD);
                    }

                    @Override
                    public String getAccountName() {
                        return CreditCard.ACCOUNTNAME;
                    }

                    @Override
                    public double getBalance() {
                        return CreditCard.BALANCE;
                    }

                    @Override
                    public double getAvailableCredit() {
                        return CreditCard.AVAILABLECREDIT;
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
                                new NameElement(
                                        NameElement.Type.FIRST_NAME, IdentityData.FIRST_NAME),
                                new NameElement(NameElement.Type.SURNAME, IdentityData.SURNAME)));
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
                destinations.add(
                        TransferDestinationPattern.createForMultiMatch(
                                AccountIdentifier.Type.IBAN, TransferDestinationPattern.ALL));
                destinations.add(
                        TransferDestinationPattern.createForMultiMatch(
                                AccountIdentifier.Type.SEPA_EUR, TransferDestinationPattern.ALL));
                transferDestinations.put(account, destinations);
            }
        }

        return new FetchTransferDestinationsResponse(transferDestinations);
    }
}
