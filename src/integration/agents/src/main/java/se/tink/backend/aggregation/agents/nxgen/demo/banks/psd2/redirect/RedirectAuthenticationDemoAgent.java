package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect;

import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.DEMO_PROVIDER_NO_ACCOUNTS_RETURNED_CASE_REGEX;
import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.DEMO_PROVIDER_ONLY_SAVINGS_AND_CHECKING_REGEX;
import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.DEMO_PROVIDER_OTP_BENEFICIARY_REGEX;
import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.OXFORD_PREPROD;
import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.OXFORD_PREPROD_CALLBACK;
import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.OXFORD_STAGING;
import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.OXFORD_STAGING_CALLBACK;
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
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.CreditCard;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.IdentityData;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.InvestmentAccounts;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.LoanAccount;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.StaticAccountUK;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.authenticator.RedirectOAuth2Authenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.beneficiary.OtpDemoCreateBeneficaryExecutor;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.beneficiary.RedirectDemoCreateBeneficaryExecutor;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.transfer.RedirectDemoPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.transfer.RedirectDemoTransferExecutor;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoAccountDefinitionGenerator;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants.MarketRegex;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.account.AccountIdentifier;
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
@AgentPisCapability(
        capabilities = {PisCapability.PIS_SEPA, PisCapability.PIS_SEPA_ICT},
        markets = {"IT"})
public final class RedirectAuthenticationDemoAgent extends NextGenerationDemoAgent
        implements RefreshTransferDestinationExecutor {
    private static final Logger log =
            LoggerFactory.getLogger(RedirectAuthenticationDemoAgent.class);

    private static final String USERNAME = "tink";

    private String provider;
    private boolean redirectToOxfordPreprod;

    public RedirectAuthenticationDemoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.provider = request.getProvider().getName();
        this.redirectToOxfordPreprod = Objects.equals("oxford-preprod", context.getClusterId());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        // Note: It's on purpose that this agent does not use the
        // AgentConfigurationController to get the callbackUri/redirectUri.
        // This is only for customers to test the callbackUri without
        // having to configure it etc.
        String callbackUri = request.getCallbackUri();

        if (OXFORD_PREPROD.equals(context.getClusterId())) {
            callbackUri = OXFORD_PREPROD_CALLBACK;
        } else if (OXFORD_STAGING.equals(context.getClusterId())) {
            callbackUri = OXFORD_STAGING_CALLBACK;
        }

        RedirectOAuth2Authenticator redirectOAuth2Authenticator =
                new RedirectOAuth2Authenticator(redirectToOxfordPreprod, callbackUri, credentials);

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
                persistentStorage
                        .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                        .filter(t -> !t.hasAccessExpired())
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);
            }
        };
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        String callbackUri = request.getCallbackUri();
        if (OXFORD_PREPROD.equals(context.getClusterId())) {
            callbackUri = OXFORD_PREPROD_CALLBACK;
        } else if (OXFORD_STAGING.equals(context.getClusterId())) {
            callbackUri = OXFORD_STAGING_CALLBACK;
        }
        RedirectOAuth2Authenticator redirectOAuth2Authenticator =
                new RedirectOAuth2Authenticator(redirectToOxfordPreprod, callbackUri, credentials);

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

        return Optional.of(new TransferController(null, transferExecutor));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        String callbackUri = request.getCallbackUri();
        if (OXFORD_PREPROD.equals(context.getClusterId())) {
            callbackUri = OXFORD_PREPROD_CALLBACK;
        } else if (OXFORD_STAGING.equals(context.getClusterId())) {
            callbackUri = OXFORD_STAGING_CALLBACK;
        }
        RedirectOAuth2Authenticator redirectOAuth2Authenticator =
                new RedirectOAuth2Authenticator(redirectToOxfordPreprod, callbackUri, credentials);

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
    public Optional<CreateBeneficiaryController> constructCreateBeneficiaryController() {
        String callbackUri = request.getCallbackUri();
        if (OXFORD_PREPROD.equals(context.getClusterId())) {
            callbackUri = OXFORD_PREPROD_CALLBACK;
        } else if (OXFORD_STAGING.equals(context.getClusterId())) {
            callbackUri = OXFORD_STAGING_CALLBACK;
        }

        RedirectOAuth2Authenticator redirectOAuth2Authenticator =
                new RedirectOAuth2Authenticator(redirectToOxfordPreprod, callbackUri, credentials);
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
        if (this.provider.matches(DEMO_PROVIDER_OTP_BENEFICIARY_REGEX)) {
            return Optional.of(
                    new CreateBeneficiaryController(
                            new OtpDemoCreateBeneficaryExecutor(
                                    credentials,
                                    thirdPartyAppAuthenticationController,
                                    supplementalInformationHelper)));
        } else {
            return Optional.of(
                    new CreateBeneficiaryController(
                            new RedirectDemoCreateBeneficaryExecutor(
                                    credentials,
                                    thirdPartyAppAuthenticationController,
                                    supplementalInformationHelper)));
        }
    }

    @Override
    public DemoInvestmentAccount getInvestmentAccounts() {
        if (this.provider.matches(DEMO_PROVIDER_NO_ACCOUNTS_RETURNED_CASE_REGEX)
                || this.provider.matches(DEMO_PROVIDER_ONLY_SAVINGS_AND_CHECKING_REGEX)) {
            return null;
        }
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
        if (this.provider.matches(DEMO_PROVIDER_NO_ACCOUNTS_RETURNED_CASE_REGEX)) {
            return null;
        }
        return DemoAccountDefinitionGenerator.getDemoSavingsAccounts(USERNAME, this.provider);
    }

    @Override
    public DemoLoanAccount getDemoLoanAccounts() {
        if (this.provider.matches(DEMO_PROVIDER_NO_ACCOUNTS_RETURNED_CASE_REGEX)
                || this.provider.matches(DEMO_PROVIDER_ONLY_SAVINGS_AND_CHECKING_REGEX)) {
            return null;
        }
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

            @Override
            public LocalDate getInitialDate() {
                return null;
            }
        };
    }

    @Override
    public List<DemoTransactionAccount> getTransactionAccounts() {
        List<DemoTransactionAccount> accounts = new ArrayList<>();
        accounts.add(
                DemoAccountDefinitionGenerator.getDemoTransactionalAccountWithZeroBalance(
                        USERNAME, this.provider));
        accounts.add(
                DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                        USERNAME, this.provider, 0));
        accounts.add(
                DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                        USERNAME, this.provider, 1));

        if (this.provider.matches(MarketRegex.UK_PROVIDERS_REGEX)) {
            if (this.provider.matches(DEMO_PROVIDER_NO_ACCOUNTS_RETURNED_CASE_REGEX)) {
                return Collections.emptyList();
            }
            accounts.add(getUkDemoAccount());
        }
        return accounts;
    }

    private DemoTransactionAccount getUkDemoAccount() {
        return new DemoTransactionAccount() {
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
        };
    }

    @Override
    public List<DemoCreditCardAccount> getCreditCardAccounts() {
        if (this.provider.matches(DEMO_PROVIDER_NO_ACCOUNTS_RETURNED_CASE_REGEX)
                || this.provider.matches(DEMO_PROVIDER_ONLY_SAVINGS_AND_CHECKING_REGEX)) {
            return Collections.EMPTY_LIST;
        }
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
        return new FetchTransferDestinationsResponse(
                DemoAccountDefinitionGenerator.generateTransferDestinations(accounts));
    }
}
