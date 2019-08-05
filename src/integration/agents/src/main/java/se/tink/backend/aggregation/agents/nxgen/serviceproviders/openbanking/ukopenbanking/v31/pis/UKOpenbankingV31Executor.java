package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis;

import java.util.ArrayList;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.UkOpenBankingPisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants.Step;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Pis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.config.DomesticPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.config.InternationalPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.config.UKPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.FundsConfirmationResponse;
import se.tink.backend.aggregation.configuration.CallbackJwtSignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

public class UKOpenbankingV31Executor implements PaymentExecutor, FetchablePaymentExecutor {

    private final UkOpenBankingPisConfig pisConfig;
    private final SoftwareStatement softwareStatement;
    private final ProviderConfiguration providerConfiguration;
    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingPis ukOpenBankingPis;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Credentials credentials;
    private final CallbackJwtSignatureKeyPair callbackJwtSignatureKeyPair;

    public UKOpenbankingV31Executor(
            UkOpenBankingPisConfig pisConfig,
            SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration,
            TinkHttpClient httpClient,
            URL wellKnownURL,
            SupplementalInformationHelper supplementalInformationHelper,
            Credentials credentials,
            CallbackJwtSignatureKeyPair callbackJwtSignatureKeyPair) {
        this.pisConfig = pisConfig;
        this.softwareStatement = softwareStatement;
        this.providerConfiguration = providerConfiguration;
        this.apiClient =
                new UkOpenBankingApiClient(
                        httpClient, softwareStatement, providerConfiguration, wellKnownURL);
        this.ukOpenBankingPis = new UkOpenBankingV31Pis(pisConfig);
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.credentials = credentials;
        this.callbackJwtSignatureKeyPair = callbackJwtSignatureKeyPair;
    }

    private UKPisConfig getConfig(Payment payment) {

        // TODO: add all possible permutations
        GenericTypeMapper<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                mapper =
                        GenericTypeMapper
                                .<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                                        genericBuilder()
                                .put(
                                        PaymentType.DOMESTIC,
                                        new Pair<>(
                                                AccountIdentifier.Type.SORT_CODE,
                                                AccountIdentifier.Type.SORT_CODE),
                                        new Pair<>(
                                                AccountIdentifier.Type.PAYM_PHONE_NUMBER,
                                                AccountIdentifier.Type.PAYM_PHONE_NUMBER))
                                .put(
                                        PaymentType.INTERNATIONAL,
                                        new Pair<>(
                                                AccountIdentifier.Type.IBAN,
                                                AccountIdentifier.Type.IBAN))
                                .build();

        PaymentType type =
                mapper.translate(payment.getCreditorAndDebtorAccountType())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                String.format(
                                                        "Cannot map Identifiers, first: %s second: %s",
                                                        payment.getCreditorAndDebtorAccountType()
                                                                .first
                                                                .toString(),
                                                        payment.getCreditorAndDebtorAccountType()
                                                                .second
                                                                .toString())));

        switch (type) {
            case DOMESTIC:
                return new DomesticPisConfig(apiClient, pisConfig);
            case SEPA:
            case INTERNATIONAL:
                return new InternationalPisConfig(apiClient, pisConfig);
            default:
                throw new IllegalStateException(String.format("Unknown type: %s", type));
        }
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        return authenticateAndCreatePisConsent(paymentRequest);
    }

    private PaymentResponse authenticateAndCreatePisConsent(PaymentRequest paymentRequest) {
        UkOpenBankingPisAuthenticator paymentAuthenticator =
                new UkOpenBankingPisAuthenticator(
                        apiClient,
                        softwareStatement,
                        providerConfiguration,
                        ukOpenBankingPis,
                        paymentRequest);

        // Do not use the real PersistentStorage because we don't want to overwrite the AIS auth
        // token.
        PersistentStorage dummyStorage = new PersistentStorage();

        OpenIdAuthenticationController openIdAuthenticationController =
                new OpenIdAuthenticationController(
                        dummyStorage,
                        supplementalInformationHelper,
                        apiClient,
                        paymentAuthenticator,
                        callbackJwtSignatureKeyPair,
                        null,
                        null,
                        credentials);

        ThirdPartyAppAuthenticationController<String> thirdPartyAppAuthenticationController =
                new ThirdPartyAppAuthenticationController<>(
                        openIdAuthenticationController, supplementalInformationHelper);

        try {
            thirdPartyAppAuthenticationController.authenticate(credentials);

        } catch (AuthenticationException | AuthorizationException e) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Authentication error.")
                    .build();
        }

        return paymentAuthenticator.getPaymentResponse();
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return getConfig(paymentRequest.getPayment()).fetchPayment(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        String consentId =
                paymentMultiStepRequest
                        .getStorage()
                        .get(UkOpenBankingV31Constants.Storage.CONSENT_ID);

        switch (paymentMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                return init(paymentMultiStepRequest);

            case UkOpenBankingV31Constants.Step.AUTHORIZE:
                return authorized(paymentMultiStepRequest, consentId);

            case UkOpenBankingV31Constants.Step.SUFFICIENT_FUNDS:
                return sufficientFunds(paymentMultiStepRequest);

            case UkOpenBankingV31Constants.Step.EXECUTE_PAYMENT:
                return executePayment(paymentMultiStepRequest);
            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }
    }

    private PaymentMultiStepResponse init(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        switch (paymentMultiStepRequest.getPayment().getStatus()) {
            case CREATED:
                // Directly executing the payment after authorizing the payment consent
                // successfully. Steps for funds check needs to be done before authorizing the
                // consent.  Step.AUTHORIZE
                return new PaymentMultiStepResponse(
                        paymentMultiStepRequest, Step.EXECUTE_PAYMENT, new ArrayList<>());
            case REJECTED:
                throw new PaymentAuthorizationException(
                        "Payment is rejected", new IllegalStateException("Payment is rejected"));
            case PENDING:
                return new PaymentMultiStepResponse(
                        paymentMultiStepRequest,
                        UkOpenBankingV31Constants.Step.SUFFICIENT_FUNDS,
                        new ArrayList<>());
            default:
                throw new IllegalStateException(
                        String.format(
                                "Unknown status %s",
                                paymentMultiStepRequest.getPayment().getStatus()));
        }
    }

    private PaymentMultiStepResponse authorized(
            PaymentMultiStepRequest paymentMultiStepRequest, String consentId)
            throws PaymentException {

        String step =
                Optional.of(
                                getConfig(paymentMultiStepRequest.getPayment())
                                        .fetchPayment(paymentMultiStepRequest))
                        .map(p -> p.getPayment().getStatus())
                        .filter(s -> s == PaymentStatus.PENDING)
                        .map(s -> UkOpenBankingV31Constants.Step.SUFFICIENT_FUNDS)
                        .orElseGet(() -> UkOpenBankingV31Constants.Step.AUTHORIZE);

        return new PaymentMultiStepResponse(paymentMultiStepRequest, step, new ArrayList<>());
    }

    private PaymentMultiStepResponse sufficientFunds(
            PaymentMultiStepRequest paymentMultiStepRequest) throws PaymentException {

        FundsConfirmationResponse response =
                getConfig(paymentMultiStepRequest.getPayment())
                        .fetchFundsConfirmation(paymentMultiStepRequest);

        if (!response.isFundsAvailable()) {
            throw new InsufficientFundsException(
                    "Insufficient funds", "", new IllegalStateException("Insufficient funds"));
        }

        return new PaymentMultiStepResponse(
                paymentMultiStepRequest,
                UkOpenBankingV31Constants.Step.EXECUTE_PAYMENT,
                new ArrayList<>());
    }

    private PaymentMultiStepResponse executePayment(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String endToEndIdentification = paymentMultiStepRequest.getPayment().getUniqueId();
        String instructionIdentification = paymentMultiStepRequest.getPayment().getUniqueId();

        PaymentResponse paymentResponse =
                getConfig(paymentMultiStepRequest.getPayment())
                        .executePayment(
                                paymentMultiStepRequest,
                                endToEndIdentification,
                                instructionIdentification);

        return new PaymentMultiStepResponse(
                paymentResponse, AuthenticationStepConstants.STEP_FINALIZE, new ArrayList<>());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }
}
