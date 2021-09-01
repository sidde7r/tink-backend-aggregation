package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums.NordeaPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreateSingleScaPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.SingleScaPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.LinkEntity;
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
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public abstract class NordeaBaseSingleScaPaymentExecutor
        implements PaymentExecutor, FetchablePaymentExecutor {
    private final NordeaBaseApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final NordeaSingleScaPaymentAuthenticator paymentAuthenticator;

    public NordeaBaseSingleScaPaymentExecutor(
            NordeaBaseApiClient apiClient,
            SessionStorage sessionStorage,
            SupplementalInformationController supplementalInformationController) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.paymentAuthenticator =
                new NordeaSingleScaPaymentAuthenticator(supplementalInformationController);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {

        CreditorEntity creditorEntity = CreditorEntity.of(paymentRequest);

        DebtorEntity debtorEntity = DebtorEntity.of(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withAmount(
                                paymentRequest
                                        .getPayment()
                                        .getExactCurrencyAmount()
                                        .getDoubleValue())
                        .withCreditor(creditorEntity)
                        .withCurrency(
                                paymentRequest
                                        .getPayment()
                                        .getExactCurrencyAmount()
                                        .getCurrencyCode())
                        .withDebtor(debtorEntity)
                        .withExternalId(paymentRequest.getPayment().getUniqueId())
                        .build();

        CreateSingleScaPaymentResponse response =
                apiClient.createSingleScaPayment(createPaymentRequest);
        savePaymentAuthorizationUrl(response);
        return toTinkPaymentResponse(createPaymentRequest);
    }

    private void savePaymentAuthorizationUrl(
            CreateSingleScaPaymentResponse createSingleScaPaymentResponse) {
        String paymentAuthorizationUrl =
                createSingleScaPaymentResponse.getResponse().getLinks().stream()
                        .filter(link -> "redirect".equals(link.getRel()))
                        .map(LinkEntity::getHref)
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No payment authorization link found"));
        sessionStorage.put(
                NordeaBaseConstants.StorageKeys.PAYMENT_AUTHORIZATION_URL, paymentAuthorizationUrl);
    }

    private String readPaymentAuthorizationUrl() {
        return Optional.ofNullable(
                        sessionStorage.get(
                                NordeaBaseConstants.StorageKeys.PAYMENT_AUTHORIZATION_URL))
                .orElseThrow(() -> new IllegalStateException("Missing authorize payment url"));
    }

    public PaymentResponse toTinkPaymentResponse(CreatePaymentRequest createPaymentRequest) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(createPaymentRequest.getCreditor().toTinkCreditor())
                        .withDebtor(createPaymentRequest.getDebtor().toTinkDebtor())
                        .withExactCurrencyAmount(
                                ExactCurrencyAmount.of(
                                        BigDecimal.valueOf(createPaymentRequest.getAmount())
                                                .setScale(2, RoundingMode.DOWN),
                                        createPaymentRequest.getCurrency()))
                        .withExecutionDate(null)
                        .withCurrency(createPaymentRequest.getCurrency())
                        .withUniqueId(createPaymentRequest.getExternalId())
                        .withStatus(PaymentStatus.CREATED)
                        .withType(PaymentType.SEPA);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        SingleScaPaymentStatusResponse singleScaPayment =
                apiClient.getSingleScaPayment(paymentRequest.getPayment().getUniqueId());
        PaymentResponse paymentResponse = PaymentResponse.of(paymentRequest);
        paymentResponse
                .getPayment()
                .setStatus(
                        NordeaPaymentStatus.mapToTinkPaymentStatus(
                                NordeaPaymentStatus.fromString(singleScaPayment.getStatus())));

        return paymentResponse;
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentStatus paymentStatus = PaymentStatus.CREATED;
        String nextStep;
        switch (paymentMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                String authorizationUrl = readPaymentAuthorizationUrl();
                paymentAuthenticator.authenticate(authorizationUrl);
                nextStep = SigningStepConstants.STEP_SIGN;
                break;

            case SigningStepConstants.STEP_SIGN:
                try {
                    getSigner().sign(paymentMultiStepRequest);
                } catch (BankIdException e) {
                    handleSigningError(e);
                }
                paymentStatus = fetch(paymentMultiStepRequest).getPayment().getStatus();
                nextStep = SigningStepConstants.STEP_FINALIZE;
                break;

            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }

        Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(payment, nextStep);
    }

    private void handleSigningError(BankIdException e) throws PaymentAuthorizationException {
        BankIdError bankIdError = e.getError();
        switch (bankIdError) {
            case CANCELLED:
                throw new PaymentAuthorizationException("Signing cancelled by the user.", e);

            case NO_CLIENT:
                throw new PaymentAuthorizationException(
                        "No client when trying to sign the payment.", e);

            case TIMEOUT:
                throw new PaymentAuthorizationException("Signing timed out.", e);

            case INTERRUPTED:
                throw new PaymentAuthorizationException("Signing interrupted.", e);

            case UNKNOWN:
            default:
                throw new PaymentAuthorizationException("Unknown problem when signing payment.", e);
        }
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
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }

    protected abstract PaymentType getPaymentType(PaymentRequest paymentRequest);

    protected abstract Signer<PaymentRequest> getSigner();
}
