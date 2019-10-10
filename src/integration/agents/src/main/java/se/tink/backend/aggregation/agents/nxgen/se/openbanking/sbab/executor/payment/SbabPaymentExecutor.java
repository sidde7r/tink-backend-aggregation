package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.TransferData;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.util.TypePair;
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
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class SbabPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final SbabApiClient apiClient;
    private final SupplementalRequester supplementalRequester;

    private static final GenericTypeMapper<PaymentType, TypePair>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper.<PaymentType, TypePair>genericBuilder()
                            .put(
                                    PaymentType.DOMESTIC,
                                    new TypePair(
                                            AccountIdentifier.Type.SE, AccountIdentifier.Type.SE))
                            .build();

    public SbabPaymentExecutor(
            SbabApiClient apiClient, SupplementalRequester supplementalRequester) {
        this.apiClient = apiClient;
        this.supplementalRequester = supplementalRequester;
    }

    private PaymentType getPaymentType(PaymentRequest paymentRequest) {
        Pair<Type, Type> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();

        return accountIdentifiersToPaymentTypeMapper
                .translate(new TypePair(accountIdentifiersKey))
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        "No PaymentType found for your AccountIdentifiers pair "
                                                + accountIdentifiersKey));
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {

        Payment payment = paymentRequest.getPayment();

        TransferData transferData =
                new TransferData.Builder()
                        .withAmount(payment.getAmount().doubleValue())
                        .withCounterPartAccount(payment.getCreditor().getAccountNumber())
                        .withCurrency(payment.getCurrency())
                        .withTransferDate(getExecutionDateOrCurrentDate(payment))
                        .build();

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder().withTransferData(transferData).build();

        CreatePaymentResponse createPaymentResponse =
                apiClient.createPayment(
                        createPaymentRequest, payment.getDebtor().getAccountNumber());

        return createPaymentResponse.toTinkPaymentResponse(
                getPaymentType(paymentRequest),
                payment.getDebtor().getAccountNumber(),
                payment.getCreditor().getAccountNumber());
    }

    private String getExecutionDateOrCurrentDate(Payment payment) {
        LocalDate executionDate =
                payment.getExecutionDate() == null ? LocalDate.now() : payment.getExecutionDate();

        return executionDate.toString();
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        return apiClient
                .getPayment(payment.getUniqueId(), payment.getDebtor().getAccountNumber())
                .toTinkPaymentResponse(
                        getPaymentType(paymentRequest),
                        payment.getDebtor().getAccountNumber(),
                        payment.getCreditor().getAccountNumber());
    }

    /**
     * SBAB doesn't support multiple steps. After the payment has been initiated successfully there
     * are two scenarios: 1. It's completed without user sign (payment between user's own accounts)
     * 2. Requires user sign with mobile bankID, which is handled by the bankIdSigner.
     */
    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentAuthorizationException {

        PaymentStatus paymentStatus = fetch(paymentMultiStepRequest).getPayment().getStatus();

        // Payment between user's own account can be "signed" on the first fetch as it doesn't
        // require a user sign. Then we want to return immediately.
        if (paymentStatus.equals(PaymentStatus.SIGNED)) {
            return getFinalisedPaymentResponse(paymentMultiStepRequest, paymentStatus);
        }

        try {
            getSigner().sign(paymentMultiStepRequest);
        } catch (AuthenticationException e) {
            if (e instanceof BankIdException) {
                BankIdError bankIdError = ((BankIdException) e).getError();
                switch (bankIdError) {
                    case CANCELLED:
                        throw new PaymentAuthorizationException(
                                "BankId signing cancelled by the user.", e);
                    case NO_CLIENT:
                        throw new PaymentAuthorizationException(
                                "No BankId client when trying to sign the payment.", e);
                    case TIMEOUT:
                        throw new PaymentAuthorizationException("BankId signing timed out.", e);
                    case INTERRUPTED:
                        throw new PaymentAuthorizationException("BankId signing interrupted.", e);
                    case UNKNOWN:
                    default:
                        throw new PaymentAuthorizationException(
                                "Unknown problem when signing payment with BankId.", e);
                }
            }
        }

        paymentStatus = fetch(paymentMultiStepRequest).getPayment().getStatus();

        return getFinalisedPaymentResponse(paymentMultiStepRequest, paymentStatus);
    }

    private PaymentMultiStepResponse getFinalisedPaymentResponse(
            PaymentMultiStepRequest paymentMultiStepRequest, PaymentStatus paymentStatus) {
        Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(paymentStatus);

        return new PaymentMultiStepResponse(
                payment, SigningStepConstants.STEP_FINALIZE, new ArrayList<>());
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
        return new PaymentListResponse(
                Optional.ofNullable(paymentListRequest)
                        .map(PaymentListRequest::getPaymentRequestList)
                        .map(Collection::stream)
                        .orElseGet(Stream::empty)
                        .map(this::fetch)
                        .collect(Collectors.toList()));
    }

    private Signer getSigner() {
        return new BankIdSigningController(supplementalRequester, new SbabBankIdSigner(this));
    }
}
